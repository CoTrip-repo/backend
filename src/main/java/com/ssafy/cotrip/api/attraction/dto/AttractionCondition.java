package com.ssafy.cotrip.api.attraction.dto;

import lombok.Builder;

@Builder
public record AttractionCondition(
        Long contentTypeId,
        Long sidoId,
        Long gugunId,
        String keyword,
        Long cursorId,
        Integer size
) {
}
