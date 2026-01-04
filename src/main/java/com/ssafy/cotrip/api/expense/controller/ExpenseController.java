package com.ssafy.cotrip.api.expense.controller;

import com.ssafy.cotrip.api.expense.dto.request.ExpenseRequest;
import com.ssafy.cotrip.api.expense.dto.response.ExpenseListResponse;
import com.ssafy.cotrip.api.expense.dto.response.ExpenseResultResponse;
import com.ssafy.cotrip.api.expense.service.ExpenseService;
import com.ssafy.cotrip.apiPayload.ApiResponse;
import com.ssafy.cotrip.global.util.SliceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/v1/plans/{planId}/expenses")
    public ApiResponse<Void> create(@PathVariable Long planId,
                                    @RequestBody @Valid ExpenseRequest request) {
        expenseService.create(planId, request);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/v1/plans/{planId}/expenses")
    public ApiResponse<SliceResponse<ExpenseListResponse, String>> list(
            @PathVariable Long planId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        SliceResponse<ExpenseListResponse, String> response = expenseService.list(planId, cursor, size);
        return ApiResponse.onSuccess(response);
    }


    @GetMapping("/v1/plans/{planId}/expenses/result")
    public ApiResponse<ExpenseResultResponse> result(@PathVariable Long planId) {
        ExpenseResultResponse response = expenseService.result(planId);
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/v1/plans/{planId}/expenses/{groupId}")
    public ApiResponse<Void> delete(@PathVariable Long planId,
                                    @PathVariable String groupId) {
        expenseService.delete(planId, groupId);
        return ApiResponse.onSuccess(null);
    }

}
