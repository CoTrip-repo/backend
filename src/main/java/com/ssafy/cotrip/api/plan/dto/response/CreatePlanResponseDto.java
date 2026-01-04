package com.ssafy.cotrip.api.plan.dto.response;

import lombok.Builder;

@Builder
public record CreatePlanResponseDto(
        Long planId,
        String code
) {}