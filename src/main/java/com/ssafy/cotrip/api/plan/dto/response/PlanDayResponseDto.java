package com.ssafy.cotrip.api.plan.dto.response;

import lombok.Builder;

@Builder
public record PlanDayResponseDto(
        Long id,
        Long planId,
        Integer day,
        String date) {
}
