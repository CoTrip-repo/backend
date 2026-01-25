package com.ssafy.cotrip.api.plan.service;

import com.ssafy.cotrip.api.plan.dto.EditInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EditLockService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final long LOCK_TTL = 30; // 30초

    // Lua Scripts
    private static final String TYPING_SCRIPT = "local val = redis.call('get', KEYS[1]) " +
            "if not val then return 0 end " +
            "local ok, info = pcall(cjson.decode, val) " +
            "if not ok then return 0 end " +
            "local userIdArg = string.gsub(ARGV[1], '\"', '') " +
            "if tostring(info.userId) == userIdArg then " +
            "    return redis.call('expire', KEYS[1], ARGV[2]) " +
            "else return 0 end";

    private static final String FINISH_SCRIPT = "local val = redis.call('get', KEYS[1]) " +
            "if not val then return 0 end " +
            "local ok, info = pcall(cjson.decode, val) " +
            "if not ok then return 0 end " +
            "local userIdArg = string.gsub(ARGV[1], '\"', '') " +
            "if tostring(info.userId) == userIdArg then " +
            "    return redis.call('del', KEYS[1]) " +
            "else return 0 end";

    /**
     * 편집 시작 (Focus)
     */
    public void startEdit(Long attractionId, Long userId, String username, Long planId) {
        String key = "editing:plan:" + planId + ":attraction:" + attractionId;
        log.info("startEdit called: attrId={}, userId={}, username={}", attractionId, userId, username);

        try {
            EditInfo editInfo = EditInfo.builder()
                    .userId(userId)
                    .username(username)
                    .startedAt(System.currentTimeMillis())
                    .build();

            // Redis에 저장 (30초 TTL) - SETNX로 원자적 락 획득
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, editInfo, LOCK_TTL, TimeUnit.SECONDS);

            if (success == null || !success) {
                log.warn("already being edited");
                return;
            }

            log.info("Redis Lock created: key={}", key);

            // 브로드캐스트: 편집 시작
            messagingTemplate.convertAndSend(
                    "/sub/plan/" + planId + "/editing",
                    Map.of(
                            "type", "START",
                            "attractionId", attractionId,
                            "userId", userId,
                            "username", username));

            log.info("User {} started editing attraction {}", username, attractionId);
        } catch (Exception e) {
            log.error("Failed to create Redis lock for attraction {}: {}", attractionId, e.getMessage(), e);
        }
    }

    /**
     * 타이핑 중 (TTL 갱신 + 실시간 전송)
     */
    public void typing(Long attractionId, Long userId, String content, Long planId) {
        String key = "editing:plan:" + planId + ":attraction:" + attractionId;
        log.info("Typing request: attrId={}, userId={}, content={}", attractionId, userId, content);

        try {
            Long result = redisTemplate.execute(
                    new DefaultRedisScript<>(TYPING_SCRIPT, Long.class),
                    Collections.singletonList(key),
                    userId.toString(), // ARGV[1]
                    String.valueOf(LOCK_TTL) // ARGV[2]
            );

            if (result != null && result == 1) {
                // 실시간 타이핑 브로드캐스트 (TTL 갱신 성공 시에만)
                messagingTemplate.convertAndSend(
                        "/sub/plan/" + planId + "/typing",
                        Map.of(
                                "attractionId", attractionId,
                                "userId", userId,
                                "content", content));
            }
        } catch (Exception e) {
            log.error("Failed to execute Lua script for typing: {}", e.getMessage(), e);
        }
    }

    /**
     * 편집 종료 (Blur 또는 저장)
     */
    public void finishEdit(Long attractionId, Long userId, Long planId) {
        String key = "editing:plan:" + planId + ":attraction:" + attractionId;

        try {
            Long result = redisTemplate.execute(
                    new DefaultRedisScript<>(FINISH_SCRIPT, Long.class),
                    Collections.singletonList(key),
                    userId.toString());

            if (result != null && result == 1) {
                // 브로드캐스트: 편집 종료
                messagingTemplate.convertAndSend(
                        "/sub/plan/" + planId + "/editing",
                        Map.of(
                                "type", "END",
                                "attractionId", attractionId));

                log.info("User {} finished editing attraction {}", userId, attractionId);
            }
        } catch (Exception e) {
            log.error("Failed to execute Lua script for finishEdit: {}", e.getMessage(), e);
        }
    }
}
