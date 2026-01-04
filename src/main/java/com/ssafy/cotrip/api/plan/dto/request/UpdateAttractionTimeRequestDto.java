package com.ssafy.cotrip.api.plan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAttractionTimeRequestDto(
        @NotNull(message = "id는 필수입니다.") Long id,

        @NotBlank(message = "newTime은 필수입니다.") String newTime) {
}
