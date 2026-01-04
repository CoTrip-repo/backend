package com.ssafy.cotrip.api.image.dto.response;

import lombok.Builder;

@Builder
public record UploadUrlResponse(
        String key,
        String putUrl,
        Long expiresInSec
) {}
