package com.ssafy.cotrip.api.hotplace.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PostRequest(
                @NotBlank(message = "title cannot be blank") String title,

                @NotBlank(message = "content cannot be blank") String content,

                Long attractionId, // Kakao 검색 시 null 가능

                KakaoPlaceData kakaoPlaceData, // 카카오 장소 데이터 (nullable)

                List<String> imageUrls) {
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
