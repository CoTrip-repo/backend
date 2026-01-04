package com.ssafy.cotrip.api.plan.dto.request;

import lombok.Builder;

@Builder
public record JoinPlanRequestDto(
   String code
) {}
