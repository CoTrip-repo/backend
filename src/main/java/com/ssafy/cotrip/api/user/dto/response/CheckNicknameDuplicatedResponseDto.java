package com.ssafy.cotrip.api.user.dto.response;

import lombok.Builder;

@Builder
public record CheckNicknameDuplicatedResponseDto(
        boolean isDuplicated
) {}