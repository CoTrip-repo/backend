package com.ssafy.cotrip.api.expense.repository;

import com.ssafy.cotrip.domain.Expense;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExpenseMapper {

    // 참가자
    List<Long> selectAllParticipantUserIds(@Param("planId") Long planId);
    boolean existsPlanParticipant(@Param("planId") Long planId, @Param("userId") Long userId);

    // create (개인별 row insert)
    int insertExpenses(@Param("rows") List<Expense> rows);

    // list (row 단위 목록) - 서비스에서 원하는 형태로 묶어서 내려주면 됨
    int countParticipants(@Param("planId") Long planId);
    List<ExpenseRow> selectExpenseRowsByPlanId(@Param("planId") Long planId);
    List<String> selectGroupIdsByPlanId(
            @Param("planId") Long planId,
            @Param("cursor") String cursor,
            @Param("limit") int limit
    );
    List<GroupPageRow> selectGroupPage(
            @Param("planId") Long planId,
            @Param("cursorDate") String cursorDate,
            @Param("cursorCreatedAt") java.time.LocalDateTime cursorCreatedAt,
            @Param("cursorGroupId") String cursorGroupId,
            @Param("limit") int limit
    );

    List<ExpenseRow> selectExpenseRowsByGroupIds(
            @Param("planId") Long planId,
            @Param("groupIds") List<String> groupIds
    );

    record GroupPageRow(String groupId, String date, java.time.LocalDateTime groupCreatedAt) {}


    // result
    Integer selectTotalAmountByPlanId(@Param("planId") Long planId);
    List<UserBurdenRow> selectUserBurdenByPlanId(@Param("planId") Long planId);

    String selectGroupIdByExpenseId(@Param("planId") Long planId, @Param("expenseId") Long expenseId);
    int softDeleteByGroupId(@Param("planId") Long planId, @Param("groupId") String groupId);

    record ExpenseRow(
            Long id,
            Long plandayId,
            String date,
            String groupId,
            String category,
            String description,
            Integer amount,
            Long userId,
            String nickname,
            java.time.LocalDateTime createdAt
    ) {}

    record UserBurdenRow(Long userId, String nickname, Integer burdenAmount) {}
}
