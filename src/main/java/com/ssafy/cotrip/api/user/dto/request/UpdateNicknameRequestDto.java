package com.ssafy.cotrip.api.user.dto.request;

import lombok.Builder;

@Builder
public record UpdateNicknameRequestDto(
        String nickname
) {}
