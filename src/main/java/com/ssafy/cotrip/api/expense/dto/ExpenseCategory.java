package com.ssafy.cotrip.api.expense.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExpenseCategory {

    FOOD("식비"),
    TRANSPORT("교통"),
    ACCOMMODATION("숙박"),
    TOUR("관광"),
    ETC("기타");

    private final String label;
}
