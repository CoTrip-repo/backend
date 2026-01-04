package com.ssafy.cotrip.api.chat.dto.response;

import com.ssafy.cotrip.api.chat.document.ChatDocument;
import com.ssafy.cotrip.api.chat.document.ChatType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatHistoryDto(
        String id,
        Long userId,
        String sender,
        ChatType type,
        String content,
        LocalDateTime timestamp
) {
    public static ChatHistoryDto from(ChatDocument entity) {
        return ChatHistoryDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .sender(entity.getSender())
                .type(entity.getType())
                .content(entity.getContent())
                .timestamp(entity.getTimestamp())
                .build();
    }
}

