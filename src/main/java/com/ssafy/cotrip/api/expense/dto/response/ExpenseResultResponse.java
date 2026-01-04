package com.ssafy.cotrip.api.expense.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ExpenseResultResponse(
        Integer totalAmount,
        List<UserShare> shares
) {
    public record UserShare(Long userId, String nickname, Integer burdenAmount) {}
}

