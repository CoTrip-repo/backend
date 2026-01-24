package com.ssafy.cotrip.api.ai.controller;

import com.ssafy.cotrip.api.ai.dto.CategoryUpdateDto;
import com.ssafy.cotrip.api.ai.service.AiService;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import com.ssafy.cotrip.security.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * AI 추천 조회
     */
    @GetMapping("/v1/plans/{planId}/ai-recommendations")
    public ApiResponse<Object> getAiRecommendations(
            @PathVariable Long planId,
            @RequestParam(name = "maxResults", required = false, defaultValue = "15") int maxResults,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("AI 추천 요청 - planId: {}, userId: {}, maxResults: {}",
                planId, userDetails.getId(), maxResults);

        Object recommendations = aiService.getAiRecommendations(planId, maxResults, userDetails.getId());

        return ApiResponse.onSuccess(recommendations);
    }

    /**
     * AI 서버로부터 카테고리 분류 결과 수신
     */
    @PutMapping("/v1/attractions/{attractionId}/ai-classification")
    public ResponseEntity<Void> updateAttractionCategoryClassification(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @PathVariable Long attractionId,
            @RequestBody @Valid CategoryUpdateDto request) {

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Missing API key");
        }

        log.info("Received AI classification for attractionId={}: categoryId={}",
                attractionId, request.contentTypeId());

        aiService.updateAttractionCategory(attractionId, request);
        return ResponseEntity.ok().build();
    }
}
