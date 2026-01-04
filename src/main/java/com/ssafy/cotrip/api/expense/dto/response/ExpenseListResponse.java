package com.ssafy.cotrip.api.expense.dto.response;

import com.ssafy.cotrip.api.expense.dto.ExpenseCategory;
import lombok.Builder;

import java.util.List;

@Builder
public record ExpenseListResponse(
        String groupId,
        String date,
        ExpenseCategory category,
        String description,
        List<String> targetNicknames,
        Integer expense
) {}

