package com.ssafy.cotrip.api.plan.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PlanDayAttractionResponseDto(
                Long id,
                Long planDayId,
                Long attractionId,
                String attractionTitle,
                String attractionImage,
                Double latitude,
                Double longitude,
                String addr1,
                String time,
                String content,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
}
