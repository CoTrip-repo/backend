package com.ssafy.cotrip.api.expense.dto.request;

import com.ssafy.cotrip.api.expense.dto.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ExpenseRequest(

        @NotNull(message = "지출 날짜는 필수입니다.")
        Long plandayId,

        @NotNull(message = "금액은 필수입니다.")
        @Positive(message = "금액은 0보다 커야 합니다.")
        Integer amount,

        @NotNull(message = "카테고리는 필수입니다.")
        ExpenseCategory category,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 20, message = "내용은 최대 20자까지 입력할 수 있습니다.")
        String description,

        List<Long> targetUserIds
) {
}
