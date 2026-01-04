package com.ssafy.cotrip.api.chat.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatCursor(
        String id,
        LocalDateTime timestamp
) {
}
