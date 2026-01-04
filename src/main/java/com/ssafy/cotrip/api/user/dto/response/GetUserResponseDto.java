package com.ssafy.cotrip.api.user.dto.response;

import lombok.Builder;

@Builder
public record GetUserResponseDto(
                String email,
                String nickname,
                Long userId,
                String role,
                String loginType) {
}
