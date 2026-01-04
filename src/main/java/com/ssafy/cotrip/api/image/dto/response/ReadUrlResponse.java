package com.ssafy.cotrip.api.image.dto.response;

import lombok.Builder;

@Builder
public record ReadUrlResponse(
        String key,
        String getUrl,
        Long expiresInSec
) {}
