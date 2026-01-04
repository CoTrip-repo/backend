package com.ssafy.cotrip.api.plan.service;

import com.ssafy.cotrip.api.plan.dto.EditInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EditLockService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final long LOCK_TTL = 30; // 30초

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

        EditInfo editInfo = (EditInfo) redisTemplate.opsForValue().get(key);
        log.info("Redis Lock Info: {}", editInfo);

        if (editInfo != null && editInfo.getUserId().equals(userId)) {
            redisTemplate.expire(key, LOCK_TTL, TimeUnit.SECONDS);

            // 실시간 타이핑 브로드캐스트
            messagingTemplate.convertAndSend(
                    "/sub/plan/" + planId + "/typing",
                    Map.of(
                            "attractionId", attractionId,
                            "userId", userId,
                            "content", content));
        }
    }

    /**
     * 편집 종료 (Blur 또는 저장)
     */
    public void finishEdit(Long attractionId, Long userId, Long planId) {
        String key = "editing:plan:" + planId + ":attraction:" + attractionId;

        EditInfo editInfo = (EditInfo) redisTemplate.opsForValue().get(key);

        // 본인이 편집 중인 것만 해제 가능
        if (editInfo != null && editInfo.getUserId().equals(userId)) {
            redisTemplate.delete(key);

            // 브로드캐스트: 편집 종료
            messagingTemplate.convertAndSend(
                    "/sub/plan/" + planId + "/editing",
                    Map.of(
                            "type", "END",
                            "attractionId", attractionId));

            log.info("User {} finished editing attraction {}", editInfo.getUsername(), attractionId);
        }
    }
}
