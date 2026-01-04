package com.ssafy.cotrip.api.plan.controller;

import com.ssafy.cotrip.api.plan.dto.response.PlanDayAttractionResponseDto;
import com.ssafy.cotrip.api.plan.service.PlanDayAttractionService;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PlanDayAttractionController {

    private final PlanDayAttractionService planDayAttractionService;

    @GetMapping("/v1/plandays/{planDayId}/attractions")
    public ApiResponse<List<PlanDayAttractionResponseDto>> getAttractionsByPlanDay(
            @PathVariable Long planDayId) {
        List<PlanDayAttractionResponseDto> attractions = planDayAttractionService.getAttractionsByPlanDay(planDayId);
        return ApiResponse.onSuccess(attractions);
    }
}
