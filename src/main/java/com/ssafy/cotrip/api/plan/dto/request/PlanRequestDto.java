package com.ssafy.cotrip.api.plan.dto.request;

import lombok.Builder;

@Builder
public record PlanRequestDto(
                String title,
                String startDate,
                String endDate,
                Integer budget) {
}
