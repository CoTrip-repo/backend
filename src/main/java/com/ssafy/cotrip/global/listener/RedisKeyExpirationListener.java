package com.ssafy.cotrip.global.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisKeyExpirationListener implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Redis key expired: {}", expiredKey);

        if (expiredKey.startsWith("editing:plan:")) {
            handleEditingLockExpiration(expiredKey);
        }
    }

    private void handleEditingLockExpiration(String key) {
        String[] parts = key.split(":");
        Long planId = Long.parseLong(parts[2]);
        Long attractionId = Long.parseLong(parts[4]);

        log.info("Editing lock expired - planId: {}, attractionId: {}", planId, attractionId);

        // 웹소켓 브로드캐스트
        messagingTemplate.convertAndSend(
                "/sub/plan/" + planId + "/editing",
                Map.of(
                        "type", "EXPIRED",
                        "attractionId", attractionId));

    }
}
