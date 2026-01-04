package com.ssafy.cotrip.api.attraction.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record AttractionSearchResponse(
        List<AttractionDto> attractions
) {
}
