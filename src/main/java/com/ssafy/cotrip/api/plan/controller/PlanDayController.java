package com.ssafy.cotrip.api.plan.controller;

import com.ssafy.cotrip.api.plan.dto.response.PlanDayResponseDto;
import com.ssafy.cotrip.api.plan.service.PlanDayService;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PlanDayController {

    private final PlanDayService planDayService;

    @GetMapping("/v1/plans/{planId}/plandays")
    public ApiResponse<List<PlanDayResponseDto>> getPlanDays(@PathVariable Long planId) {
        List<PlanDayResponseDto> planDays = planDayService.getPlanDaysByPlanId(planId);
        return ApiResponse.onSuccess(planDays);
    }
}
