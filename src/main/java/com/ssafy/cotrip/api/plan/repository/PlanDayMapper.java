package com.ssafy.cotrip.api.plan.repository;

import com.ssafy.cotrip.domain.PlanDay;

import java.util.List;

public interface PlanDayMapper {
    void insert(PlanDay planDay);

    void update(PlanDay planDay);

    void delete(Long id);

    PlanDay findById(Long id);

    List<PlanDay> findByPlanId(Long planId);
}
