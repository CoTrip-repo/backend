package com.ssafy.cotrip.domain;

import com.ssafy.cotrip.domain.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth extends BaseEntity {
    private Long id;
    private String provider;
    private String providerId;
    private Long userId;
}
