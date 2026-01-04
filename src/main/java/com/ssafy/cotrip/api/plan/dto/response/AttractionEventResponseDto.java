package com.ssafy.cotrip.api.plan.dto.response;

import lombok.Builder;

@Builder
public record AttractionEventResponseDto(
        String type,
        PlanDayAttractionResponseDto data,
        Long deletedId) {
}
