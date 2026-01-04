package com.ssafy.cotrip.api.plan.service;

import com.ssafy.cotrip.api.plan.dto.response.PlanDayResponseDto;
import com.ssafy.cotrip.api.plan.repository.PlanDayMapper;
import com.ssafy.cotrip.domain.PlanDay;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanDayService {
    private final PlanDayMapper planDayMapper;

    public List<PlanDayResponseDto> getPlanDaysByPlanId(Long planId) {
        List<PlanDay> planDays = planDayMapper.findByPlanId(planId);
        return planDays.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PlanDayResponseDto convertToDto(PlanDay planDay) {
        return PlanDayResponseDto.builder()
                .id(planDay.getId())
                .planId(planDay.getPlanId())
                .day(planDay.getDay())
                .date(planDay.getDate())
                .build();
    }
}
