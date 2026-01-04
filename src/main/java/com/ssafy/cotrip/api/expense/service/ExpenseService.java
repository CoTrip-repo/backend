package com.ssafy.cotrip.api.expense.service;

import com.ssafy.cotrip.api.expense.dto.ExpenseCategory;
import com.ssafy.cotrip.api.expense.dto.request.ExpenseRequest;
import com.ssafy.cotrip.api.expense.dto.response.ExpenseListResponse;
import com.ssafy.cotrip.api.expense.dto.response.ExpenseResultResponse;
import com.ssafy.cotrip.api.expense.repository.ExpenseMapper;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.ExpenseHandler;
import com.ssafy.cotrip.domain.Expense;
import com.ssafy.cotrip.global.util.SliceResponse;
import com.ssafy.cotrip.global.util.SliceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseMapper expenseMapper;
    private final SliceService sliceService;

    public void create(Long planId, ExpenseRequest request) {
        List<Long> targets = resolveTargets(planId, request.targetUserIds());
        validateTargetsAreParticipants(planId, targets);

        int n = targets.size();
        if (n <= 0) throw new ExpenseHandler(ErrorStatus.TARGET_USER_REQUIRED);

        int share = floorToTen(request.amount() / n);

        String groupId = UUID.randomUUID().toString();

        List<Expense> rows = targets.stream()
                .map(userId -> Expense.builder()
                        .planId(planId)
                        .plandayId(request.plandayId())
                        .userId(userId)
                        .groupId(groupId)
                        .amount(share)
                        .category(request.category())
                        .description(request.description())
                        .build())
                .toList();

        expenseMapper.insertExpenses(rows);
    }

    public void delete(Long planId, String groupId) {
        int deleted = expenseMapper.softDeleteByGroupId(planId, groupId);
        if (deleted == 0) {
            throw new ExpenseHandler(ErrorStatus.EXPENSE_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public SliceResponse<ExpenseListResponse, String> list(Long planId, String cursor, int size) {
        int participantCount = expenseMapper.countParticipants(planId);

        Cursor c = parseCursor(cursor);

        // 1) 그룹 단위로 size+1개 조회해서 hasNext 판단
        List<ExpenseMapper.GroupPageRow> groups = expenseMapper.selectGroupPage(
                planId,
                c.date(),
                c.createdAt(),
                c.groupId(),
                size + 1
        );

        boolean hasNext = groups.size() > size;
        if (hasNext) groups = groups.subList(0, size);

        if (groups.isEmpty()) {
            return SliceResponse.<ExpenseListResponse, String>builder()
                    .content(List.of())
                    .hasNext(false)
                    .nextCursor(null)
                    .build();
        }

        List<String> groupIds = groups.stream().map(ExpenseMapper.GroupPageRow::groupId).toList();

        // 2) 해당 그룹들의 row를 한번에 가져오고, groupId로 묶기
        List<ExpenseMapper.ExpenseRow> rows = expenseMapper.selectExpenseRowsByGroupIds(planId, groupIds);

        Map<String, List<ExpenseMapper.ExpenseRow>> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        ExpenseMapper.ExpenseRow::groupId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 3) groups 순서(정렬된 순서)대로 응답 만들기
        List<ExpenseListResponse> result = new ArrayList<>();

        for (ExpenseMapper.GroupPageRow gmeta : groups) {
            List<ExpenseMapper.ExpenseRow> g = grouped.get(gmeta.groupId());
            if (g == null || g.isEmpty()) continue;

            ExpenseMapper.ExpenseRow first = g.get(0);

            int expense = first.amount(); // 1인 부담금

            List<String> targetNicknames;
            if (g.size() == participantCount) {
                targetNicknames = List.of("전원");
            } else {
                targetNicknames = g.stream()
                        .map(ExpenseMapper.ExpenseRow::nickname)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();
            }

            result.add(ExpenseListResponse.builder()
                    .groupId(first.groupId())
                    .date(first.date())
                    .category(ExpenseCategory.valueOf(first.category()))
                    .description(first.description())
                    .targetNicknames(targetNicknames)
                    .expense(expense)
                    .build());
        }

        // 4) nextCursor = 마지막 그룹의 (date|groupCreatedAt|groupId)
        ExpenseMapper.GroupPageRow last = groups.get(groups.size() - 1);
        String nextCursor = buildCursor(last.date(), last.groupCreatedAt(), last.groupId());

        return SliceResponse.<ExpenseListResponse, String>builder()
                .content(result)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    private String buildCursor(String date, java.time.LocalDateTime createdAt, String groupId) {
        return date + "|" + createdAt.toString() + "|" + groupId;
    }

    private Cursor parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return new Cursor(null, null, null);
        }
        String[] parts = cursor.split("\\|", 3);
        if (parts.length != 3) {
            throw new ExpenseHandler(ErrorStatus.INVALID_CURSOR);
        }
        return new Cursor(
                parts[0],
                java.time.LocalDateTime.parse(parts[1]),
                parts[2]
        );
    }

    @Transactional(readOnly = true)
    public ExpenseResultResponse result(Long planId) {
        Integer total = expenseMapper.selectTotalAmountByPlanId(planId);
        List<ExpenseMapper.UserBurdenRow> rows = expenseMapper.selectUserBurdenByPlanId(planId);

        return ExpenseResultResponse.builder()
                .totalAmount(total)
                .shares(rows.stream()
                        .map(r -> new ExpenseResultResponse.UserShare(r.userId(), r.nickname(), r.burdenAmount()))
                        .toList())
                .build();
    }

    private List<Long> resolveTargets(Long planId, List<Long> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return expenseMapper.selectAllParticipantUserIds(planId);
        }
        return targetUserIds.stream().distinct().toList();
    }

    private void validateTargetsAreParticipants(Long planId, List<Long> targets) {
        for (Long uid : targets) {
            if (!expenseMapper.existsPlanParticipant(planId, uid)) {
                throw new ExpenseHandler(ErrorStatus.INVALID_TARGET_USER);
            }
        }
    }

    private int floorToTen(int v) {
        return (v / 10) * 10;
    }

    private record Cursor(String date, java.time.LocalDateTime createdAt, String groupId) {}
}
