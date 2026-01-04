package com.ssafy.cotrip.api.plan.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GetPlanResponseDto(
                Long planId,
                Long leaderId,
                String code,
                String title,
                String startDate,
                String endDate,
                Integer budget,
                List<UserDto> users,
                List<PlanDayWithAttractions> days) {
        @Builder
        public record UserDto(
                        Long userId,
                        String nickname,
                        boolean isDeleted // 탈퇴한 유저인지?
        ) {
        }

        @Builder
        public record PlanDayWithAttractions(
                        Long planDayId,
                        Integer day,
                        String date,
                        List<PlanDayAttractionResponseDto> attractions) {
        }
}
