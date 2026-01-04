package com.ssafy.cotrip.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * HTTP 클라이언트 설정
 * - RestClient: 동기 HTTP 호출 (AI 추천 조회)
 * - WebClient: 비동기 HTTP 호출 (임베딩 추가)
 */
@Configuration
public class HttpClientConfig {

    @Value("${ai.server.url}")
    @NonNull
    private String aiServerUrl;

    @Value("${ai.api.key}")
    @NonNull
    private String aiApiKey;

    /**
     * RestClient Bean (동기)
     * AI 추천 조회 등 사용자가 결과를 기다리는 경우 사용
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(aiServerUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-API-Key", aiApiKey)
                .requestFactory(new org.springframework.http.client.ReactorClientHttpRequestFactory(
                        reactor.netty.http.client.HttpClient.create()
                                .responseTimeout(java.time.Duration.ofSeconds(180)) // 180초 타임아웃 (3분 - LLM Re-ranking
                                                                                    // 충분한 여유)
                ))
                .build();
    }

    /**
     * WebClient Bean (비동기)
     * 임베딩 추가 등 Fire-and-forget 방식으로 사용
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(aiServerUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-API-Key", aiApiKey)
                .build();
    }
}
