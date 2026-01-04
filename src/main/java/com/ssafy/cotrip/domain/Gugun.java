package com.ssafy.cotrip.domain;

import com.ssafy.cotrip.domain.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Gugun extends BaseEntity {
    private Long id;
    private Integer gugunCode;
    private String gugunName;
    private Long sidoId;      // FK -> sidos.id
}
