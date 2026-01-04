package com.ssafy.cotrip.api.user.dto.request;

import lombok.Builder;

@Builder
public record LoginRequestDto(
        String email,
        String password
) {}
