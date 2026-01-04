package com.ssafy.cotrip.api.plan.repository;

import com.ssafy.cotrip.domain.PlanParticipant;
import com.ssafy.cotrip.domain.User;

import java.util.List;

public interface PlanParticipantMapper {
    void insert(PlanParticipant planParticipant);

    void delete(Long planId); // plan 전체 삭제 시 사용

    void deleteByPlanIdAndUserId(Long planId, Long userId); // 특정 사용자가 plan 나갈 때 사용

    boolean existsByPlanIdAndUserId(Long planId, Long userId);

    List<User> findUsersByPlanId(Long planId);
}
