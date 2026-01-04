package com.ssafy.cotrip.api.plan.controller;

import com.ssafy.cotrip.api.plan.dto.request.JoinPlanRequestDto;
import com.ssafy.cotrip.api.plan.dto.request.PlanRequestDto;
import com.ssafy.cotrip.api.plan.dto.response.CreatePlanResponseDto;
import com.ssafy.cotrip.api.plan.dto.response.PlanDto;
import com.ssafy.cotrip.api.plan.dto.response.GetPlanResponseDto;
import com.ssafy.cotrip.api.plan.dto.response.JoinPlanResponseDto;
import com.ssafy.cotrip.api.plan.service.PlanService;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import com.ssafy.cotrip.global.util.SliceResponse;
import com.ssafy.cotrip.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;

    @PostMapping("/v1/plans")
    public ApiResponse<CreatePlanResponseDto> insertPlan(@RequestBody PlanRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CreatePlanResponseDto responseDto = planService.insertPlan(requestDto.title(), requestDto.startDate(),
                requestDto.endDate(), userDetails.getId());
        return ApiResponse.onSuccess(responseDto);
    }

    // 목록조회
    @GetMapping("/v1/plans")
    public ApiResponse<SliceResponse<PlanDto, Long>> getPlans(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", required = false, defaultValue = "6") int size) {
        SliceResponse<PlanDto, Long> responseDto = planService.getPlans(userDetails.getId(), cursorId,
                size);
        return ApiResponse.onSuccess(responseDto);
    }

    // 단건조회
    @GetMapping("/v1/plans/{id}")
    public ApiResponse<GetPlanResponseDto> getPlan(@PathVariable("id") Long planId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GetPlanResponseDto responseDto = planService.getPlan(planId, userDetails.getId());
        return ApiResponse.onSuccess(responseDto);
    }

    // 수정은 리더만 가능
    @PatchMapping("/v1/plans/{id}")
    public ApiResponse updatePlan(@RequestBody PlanRequestDto requestDto, @PathVariable("id") Long planId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        planService.updatePlan(requestDto.title(), requestDto.startDate(), requestDto.endDate(), requestDto.budget(),
                planId, userDetails.getId());
        return ApiResponse.onSuccess(null);
    }

    // 삭제는 리더만 가능
    @DeleteMapping("/v1/plans/{id}")
    public ApiResponse deletePlan(@PathVariable("id") Long planId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        planService.deletePlan(planId, userDetails.getId());
        return ApiResponse.onSuccess(null);
    }

    // 코드로 참여
    @PostMapping("/v1/plans/code")
    public ApiResponse<JoinPlanResponseDto> joinPlan(@RequestBody JoinPlanRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        JoinPlanResponseDto responseDto = planService.joinPlan(requestDto.code(), userDetails.getId());
        return ApiResponse.onSuccess(responseDto);
    }

    // 방 나가기 (리더 제외한 참여자)
    @DeleteMapping("/v1/plans/{planId}/users/{userId}")
    public ApiResponse leavePlan(@PathVariable Long planId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        planService.leavePlan(planId, userDetails.getId());
        return ApiResponse.onSuccess(null);
    }
}
