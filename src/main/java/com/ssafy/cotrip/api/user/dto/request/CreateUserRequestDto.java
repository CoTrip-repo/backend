package com.ssafy.cotrip.api.user.dto.request;

import lombok.Builder;

@Builder
public record CreateUserRequestDto(
        String email,
        String password,
        String nickname
) {}
