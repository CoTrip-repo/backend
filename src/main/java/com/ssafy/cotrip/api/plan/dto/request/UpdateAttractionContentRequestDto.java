package com.ssafy.cotrip.api.plan.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateAttractionContentRequestDto(
        @NotNull(message = "id는 필수입니다.") Long id,

        String content) {
}
