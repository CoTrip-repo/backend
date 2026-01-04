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
public class PlanDay extends BaseEntity {
    private Long id;
    private Integer day;
    private String date;

    private Long planId;       // FK -> plan.id
}
