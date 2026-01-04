package com.ssafy.cotrip.api.chat.dto.request;

import com.ssafy.cotrip.api.chat.document.ChatType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ChatMessagePub(
        @NotNull(message = "planId는 필수 입력 값입니다.")
        Long planId,

        @NotNull(message = "ChatType은 필수 입력 값입니다.")
        ChatType type,

        String content
) {
}
