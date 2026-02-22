package com.ssafy.cotrip.api.ai.service;

import com.ssafy.cotrip.api.ai.dto.CategoryUpdateDto;
import com.ssafy.cotrip.api.attraction.dto.response.AttractionDto;
import com.ssafy.cotrip.global.annotation.RequirePlanMembership;
import com.ssafy.cotrip.api.attraction.repository.AttractionMapper;
import com.ssafy.cotrip.api.chat.service.ChatService;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.AiHandler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

    private final AttractionMapper attractionMapper;
    private final ChatService chatService;
    private final RestClient restClient;
    private final WebClient webClient;

    @RequirePlanMembership
    @CircuitBreaker(name = "aiServer", fallbackMethod = "getAiRecommendationsFallback")
    @Retry(name = "aiServer")
    public Object getAiRecommendations(Long planId, int maxResults, Long userId) {
        // 채팅 기록 조회
        List<?> chatHistory = chatService.getChatHistoryForAi(planId, 500);

        // AI 서버 요청 생성
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

    // 간단한 인메모리 캐시 (서버 재시작 시 초기화)
    private List<Long> candidateIdsCache;
    private long lastCacheUpdateTime = 0;
    private static final long CACHE_DURATION_MS = 3600000; // 1시간

    public Object getAiRecommendationsFallback(Long planId, int maxResults, Long userId, Throwable ex) {
        log.warn("Circuit Breaker Fallback 호출 - planId: {}, error: {}", planId, ex.getMessage());

        // 후보 ID 목록 조회 (캐싱)
        updateCandidateCacheIfNeeded();

        if (candidateIdsCache.isEmpty()) {
            return Map.of("fallback", true, "recommendations", List.of());
        }

        // 랜덤 ID 선택
        Set<Long> selectedIds = new java.util.HashSet<>();
        Random random = new Random();
        int totalCandidates = candidateIdsCache.size();
        int targetCount = Math.min(maxResults, totalCandidates);

        while (selectedIds.size() < targetCount) {
            int index = random.nextInt(totalCandidates);
            selectedIds.add(candidateIdsCache.get(index));
        }

        // 선택된 ID로 정보 조회
        List<AttractionDto> popularAttractions = attractionMapper
                .findAttractionsByIds(new java.util.ArrayList<>(selectedIds));

        return Map.of(
                "recommendations", popularAttractions,
                "fallback", true,
                "message", "AI 추천 서비스를 일시적으로 사용할 수 없어 랜덤으로 여행지를 추천해드립니다.",
                "planId", planId);
    }

    private void updateCandidateCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (candidateIdsCache == null || candidateIdsCache.isEmpty()
                || (currentTime - lastCacheUpdateTime > CACHE_DURATION_MS)) {
            synchronized (this) {
                if (candidateIdsCache == null || candidateIdsCache.isEmpty()
                        || (currentTime - lastCacheUpdateTime > CACHE_DURATION_MS)) {
                    log.info("Attraction ID Cache 업데이트 시작...");
                    candidateIdsCache = attractionMapper.findCandidateAttractionIds();
                    lastCacheUpdateTime = currentTime;
                    log.info("Attraction ID Cache 업데이트 완료: {} 개", candidateIdsCache.size());
                }
            }
        }
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
}
