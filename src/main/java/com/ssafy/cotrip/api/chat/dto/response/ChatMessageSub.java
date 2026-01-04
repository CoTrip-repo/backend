package com.ssafy.cotrip.api.chat.dto.response;

import com.ssafy.cotrip.api.chat.document.ChatType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageSub(
        String id,
        Long planId,
        Long userId,
        String sender,
        String content,
        ChatType type,
        LocalDateTime timestamp
) {
}
