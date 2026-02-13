package com.ssafy.cotrip.api.test.controller;

import com.ssafy.cotrip.api.ai.service.AiService;
import com.ssafy.cotrip.api.plan.dto.request.PlanRequestDto;
import com.ssafy.cotrip.api.plan.dto.response.CreatePlanResponseDto;
import com.ssafy.cotrip.api.plan.dto.response.PlanDto;
import com.ssafy.cotrip.api.plan.service.PlanService;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import com.ssafy.cotrip.global.util.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * K6 부하 테스트용 컨트롤러 (인증 불필요)
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class LoadTestController {

    private final AiService aiService;
    private final PlanService planService;

    /**
     * Step 1: AI Fallback 테스트용 엔드포인트
     * GET /api/test/plans/{planId}/ai-recommendations
     */
    @GetMapping("/plans/{planId}/ai-recommendations")
    public ApiResponse<Object> testAiRecommendations(
            @PathVariable Long planId,
            @RequestParam(name = "maxResults", required = false, defaultValue = "15") int maxResults) {

        log.debug("K6 Test - AI recommendations: planId={}, maxResults={}", planId, maxResults);

        // 테스트용 userId (실제 존재하는 사용자 ID로 변경 필요)
        Long testUserId = 1L;

        try {
            Object recommendations = aiService.getAiRecommendations(planId, maxResults, testUserId);
            return ApiResponse.onSuccess(recommendations);
        } catch (Exception e) {
            log.warn("AI recommendations failed, using fallback: {}", e.getMessage());
            // Fallback 직접 호출
            Object fallback = aiService.getAiRecommendationsFallback(planId, maxResults, testUserId, e);
            return ApiResponse.onSuccess(fallback);
        }
    }

    /**
     * Step 2: Plan 생성 테스트용 엔드포인트
     * POST /api/test/plans
     */
    @PostMapping("/plans")
    public ApiResponse<CreatePlanResponseDto> testCreatePlan(
            @RequestBody PlanRequestDto request) {

        log.debug("K6 Test - Create plan: {}", request.title());

        // 테스트용 userId
        Long testUserId = 1L;

        CreatePlanResponseDto response = planService.insertPlan(
                request.title(),
                request.startDate(),
                request.endDate(),
                testUserId);

        return ApiResponse.onSuccess(response);
    }

    /**
     * Step 3: Plan 목록 조회 테스트용 엔드포인트
     * GET /api/test/plans
     */
    @GetMapping("/plans")
    public ApiResponse<SliceResponse<PlanDto, Long>> testGetPlans(
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size) {

        log.debug("K6 Test - Get plans: cursorId={}, size={}", cursorId, size);

        // 테스트용 userId
        Long testUserId = 1L;

        SliceResponse<PlanDto, Long> response = planService.getPlans(testUserId, cursorId, size);

        return ApiResponse.onSuccess(response);
    }

    /**
     * 헬스체크 엔드포인트
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.onSuccess("K6 Load Test endpoints are ready!");
    }
}
