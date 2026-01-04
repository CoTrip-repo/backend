package com.ssafy.cotrip.domain;

import com.ssafy.cotrip.domain.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanDayAttraction extends BaseEntity {
    private Long id;
    private String content;
    private String time;
    private Long attractionId;
    private Long plandayId;

    // Join된 데이터 (DB 컬럼 아님)
    private String attractionTitle;
    private Double latitude;
    private Double longitude;
    private String addr1;
}