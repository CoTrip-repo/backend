package com.ssafy.cotrip.domain;

import com.ssafy.cotrip.domain.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Plan extends BaseEntity {
    private Long id;
    private String code;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer budget; // 예산 (선택)

    private Long leaderId; // FK -> users.id

    public void assignCode(String code) {
        this.code = code;
    }
}
