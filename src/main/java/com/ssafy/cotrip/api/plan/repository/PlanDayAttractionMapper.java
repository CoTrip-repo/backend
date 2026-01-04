package com.ssafy.cotrip.api.plan.repository;

import com.ssafy.cotrip.domain.PlanDayAttraction;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlanDayAttractionMapper {
    void insert(PlanDayAttraction planDayAttraction);

    void update(PlanDayAttraction planDayAttraction);

    void delete(Long id);

    PlanDayAttraction findById(Long id);

    List<PlanDayAttraction> findByPlanDayId(Long planDayId);

    boolean existsByPlanDayIdAndTime(
            @Param("planDayId") Long planDayId,
            @Param("time") String time,
            @Param("excludeId") Long excludeId);

    int countByPlanDayId(Long planDayId);
}
