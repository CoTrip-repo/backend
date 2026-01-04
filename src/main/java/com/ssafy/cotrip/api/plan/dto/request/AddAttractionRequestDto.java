package com.ssafy.cotrip.api.plan.dto.request;

/**
 * 일정에 장소 추가 요청 DTO
 * - attractionId: DB에 이미 있는 장소 ID (기존 방식)
 * - kakaoPlaceData: 카카오 검색 결과 (DB에 없으면 자동 저장)
 */
public record AddAttractionRequestDto(
        Long planDayId,
        Long attractionId,
        KakaoPlaceData kakaoPlaceData, // 카카오 장소 데이터 (nullable)
        String time,
        String content) {

    public record KakaoPlaceData(
            String id,
            String placeName,
            String categoryName,
            String phone,
            String addressName,
            String roadAddressName,
            String x, // 경도
            String y, // 위도
            String placeUrl) {
    }
}
