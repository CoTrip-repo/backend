package com.ssafy.cotrip.global.aop;

import com.ssafy.cotrip.api.plan.repository.PlanParticipantMapper;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.PlanHandler;
import com.ssafy.cotrip.global.annotation.RequirePlanMembership;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PlanMembershipAspect {

    private final PlanParticipantMapper planParticipantMapper;

    @Before("@annotation(requirePlanMembership)")
    public void checkMembership(JoinPoint joinPoint, RequirePlanMembership requirePlanMembership) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();

        Long planId = (Long) getParamValue(paramNames, args, requirePlanMembership.planIdParam());
        Long userId = (Long) getParamValue(paramNames, args, requirePlanMembership.userIdParam());

        if (planId == null || userId == null) {
            throw new PlanHandler(ErrorStatus._BAD_REQUEST, "잘못된 요청 파라미터입니다.");
        }

        boolean exists = planParticipantMapper.existsByPlanIdAndUserId(planId, userId);
        if (!exists) {
            throw new PlanHandler(ErrorStatus._FORBIDDEN, "Plan 멤버만 접근할 수 있습니다.");
        }
    }

    private Object getParamValue(String[] paramNames, Object[] args, String targetParamName) {
        return IntStream.range(0, paramNames.length)
                .filter(i -> paramNames[i].equals(targetParamName))
                .mapToObj(i -> args[i])
                .findFirst()
                .orElse(null);
    }
}
