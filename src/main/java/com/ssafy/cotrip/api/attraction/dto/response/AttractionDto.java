package com.ssafy.cotrip.api.attraction.dto.response;

import lombok.Builder;

@Builder
public record AttractionDto(
        Long id,
        Long contentId,
        String contentTypeName,
        String title,
        String image1,
        String image2,
        Integer mapLevel,
        Double latitude,
        Double longitude,
        String tel,
        String addr1,
        String addr2,
        String homepage,
        String overview
) {
}
