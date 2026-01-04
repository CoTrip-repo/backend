package com.ssafy.cotrip.api.plan.dto.response;

import lombok.Builder;

@Builder
public record PlanDto(
        Long planId,
        Long leaderId,
        int num, // 참여한 인원 수
        String title,
        String startDate,
        String endDate
) {}


