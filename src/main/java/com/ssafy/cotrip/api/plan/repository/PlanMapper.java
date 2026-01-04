package com.ssafy.cotrip.api.plan.repository;

import com.ssafy.cotrip.domain.Plan;

import java.time.LocalDate;
import java.util.List;

public interface PlanMapper {
    void insert(Plan plan);

    void update(String title, LocalDate startDate, LocalDate endDate, Integer budget, Long planId);

    void delete(Long planId);

    Plan findByPlanId(Long planId);

    List<Plan> findByUserId(Long userId);

    List<Plan> findByUserIdWithCursor(Long userId, Long cursorId, int size);

    int getPlanParticipantCount(Long planId);

    Plan findByCode(String code);
}
