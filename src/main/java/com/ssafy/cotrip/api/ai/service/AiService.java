package com.ssafy.cotrip.api.ai.service;

import com.ssafy.cotrip.api.ai.dto.CategoryUpdateDto;
import com.ssafy.cotrip.api.attraction.repository.AttractionMapper;
import com.ssafy.cotrip.api.chat.service.ChatService;
import com.ssafy.cotrip.api.plan.repository.PlanParticipantMapper;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.AiHandler;
import com.ssafy.cotrip.apiPayload.exception.handler.PlanHandler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AI 서버 연동 서비스
 * - RestClient: 동기 호출 (AI 추천 조회)
 * - WebClient: 비동기 호출 (임베딩 추가, 카테고리 분류)
 * - Resilience4j: Circuit Breaker + Retry 적용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final PlanParticipantMapper planParticipantMapper;
    private final AttractionMapper attractionMapper;
    private final ChatService chatService;
    private final RestClient restClient;
    private final WebClient webClient;

    @CircuitBreaker(name = "aiServer", fallbackMethod = "getAiRecommendationsFallback")
    @Retry(name = "aiServer")
    public Object getAiRecommendations(Long planId, int maxResults, Long userId) {
        // 1. Plan 멤버십 확인
        verifyMembership(planId, userId);

        // 2. 채팅 기록 조회
        List<?> chatHistory = chatService.getChatHistoryForAi(planId, 500);

        // 3. AI 서버 요청 생성
        Map<String, Object> request = Map.of(
                "plan_id", planId,
                "chat_history", chatHistory,
                "max_results", maxResults);

        log.info("Calling AI server for planId: {}", planId);

        return restClient.post()
                .uri("/api/recommendations")
                .body(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), (req, res) -> {
                    log.error("AI Server 4xx error: {}", res.getStatusCode());
                    throw new AiHandler(ErrorStatus._BAD_REQUEST,
                            "AI 서버 요청 오류: 잘못된 요청 데이터");
                })
                .onStatus(status -> status.is5xxServerError(), (req, res) -> {
                    log.error("AI Server 5xx error: {}", res.getStatusCode());
                    throw new AiHandler(ErrorStatus._INTERNAL_SERVER_ERROR,
                            "AI 서버 내부 오류");
                })
                .body(Object.class);
    }

    public Object getAiRecommendationsFallback(Long planId, int maxResults, Long userId, Throwable ex) {
        log.warn("Circuit Breaker Fallback 호출 - planId: {}, error: {}", planId, ex.getMessage());

        // 인기 여행지 조회 (이미지와 설명이 있는 관광지 랜덤 조회)
        List<?> popularAttractions = attractionMapper.findPopularAttractions(maxResults);

        return Map.of(
                "recommendations", popularAttractions,
                "fallback", true,
                "message", "AI 추천 서비스를 일시적으로 사용할 수 없어 랜덤으로 여행지를 추천해드립니다.",
                "planId", planId);
    }

    public void processNewAttraction(Long attractionId) {
        log.info("Processing new attraction: {}", attractionId);

        webClient.post()
                .uri("/api/attractions/{id}/embeddings", attractionId)
                .retrieve()
                .bodyToMono(Void.class)
                .retry(2) // 비동기 호출도 재시도 추가
                .subscribe(
                        result -> log.info("Attraction processed: {}", attractionId),
                        error -> log.error("Attraction processing failed: {}", attractionId, error));
    }

    @Transactional
    public void updateAttractionCategory(Long attractionId, CategoryUpdateDto request) {
        try {
            attractionMapper.updateContentType(attractionId, request.contentTypeId());
            log.info("Received AI classification for attractionId={}: categoryId={}",
                    attractionId, request.contentTypeId());
        } catch (Exception e) {
            log.error("Failed to update category for attractionId: {}", attractionId, e);
            throw e;
        }
    }

    private void verifyMembership(Long planId, Long userId) {
        boolean exist = planParticipantMapper.existsByPlanIdAndUserId(planId, userId);
        if (!exist) {
            throw new PlanHandler(ErrorStatus._FORBIDDEN, "Plan 멤버만 접근할 수 있습니다.");
        }
    }
}
