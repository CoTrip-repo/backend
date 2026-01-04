package com.ssafy.cotrip.domain;

import com.ssafy.cotrip.api.expense.dto.ExpenseCategory;
import com.ssafy.cotrip.domain.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Expense extends BaseEntity {
    private Long id; // AUTO_INCREMENT PRIMARY KEY

    private Long planId; // FK -> plans.id
    private Long userId; // FK -> users.id
    private Long plandayId; // FK -> plandays.id

    private String groupId;

    private Integer amount; // 금액
    private ExpenseCategory category; // 카테고리
    private String description; // 설명
}
