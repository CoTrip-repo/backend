package com.ssafy.cotrip.api.plan.service;

import com.ssafy.cotrip.api.chat.document.ChatType;
import com.ssafy.cotrip.api.chat.dto.request.ChatMessagePub;
import com.ssafy.cotrip.api.chat.service.ChatService;
import com.ssafy.cotrip.api.plan.dto.response.CreatePlanResponseDto;
import com.ssafy.cotrip.api.plan.dto.response.GetPlanResponseDto;
import com.ssafy.cotrip.api.plan.dto.response.JoinPlanResponseDto;
import com.ssafy.cotrip.api.plan.dto.response.PlanDto;
import com.ssafy.cotrip.api.plan.repository.PlanDayMapper;
import com.ssafy.cotrip.api.plan.repository.PlanMapper;
import com.ssafy.cotrip.api.plan.repository.PlanParticipantMapper;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.PlanHandler;
import com.ssafy.cotrip.domain.Plan;
import com.ssafy.cotrip.domain.PlanDay;
import com.ssafy.cotrip.domain.PlanParticipant;
import com.ssafy.cotrip.domain.User;
import com.ssafy.cotrip.global.annotation.RequirePlanMembership;
import com.ssafy.cotrip.global.util.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.cotrip.api.plan.dto.response.PlanDayAttractionResponseDto;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlanService {
    private final PlanMapper planMapper;
    private final PlanParticipantMapper planParticipantMapper;
    private final ChatService chatService;
    private final PlanDayMapper planDayMapper;
    private final PlanDayAttractionService planDayAttractionService;

    @Transactional
    public CreatePlanResponseDto insertPlan(String title, String startDate, String endDate, Long id) {
        Plan plan = Plan.builder()
                .title(title)
                .startDate(LocalDate.parse(startDate))
                .endDate(LocalDate.parse(endDate))
                .leaderId(id)
                .build();

        // todo - 중복값 넣어서 테스트해보기
        int maxRetry = 5;
        for (int i = 1; i <= maxRetry; i++) {
            plan.assignCode(generateCode());

            try {
                planMapper.insert(plan);
                break;
            } catch (DuplicateKeyException e) {
                if (i == maxRetry) {
                    log.info("DuplicateKeyException 발생");
                    throw new PlanHandler(ErrorStatus._BAD_REQUEST,
                            "Plan 코드 생성 중에 문제가 생겼습니다. 다시 시도해주세요.");
                }
            }
        }

        PlanParticipant userPlan = PlanParticipant.builder()
                .planId(plan.getId())
                .userId(id)
                .build();

        planParticipantMapper.insert(userPlan);

        // PlanDay 자동 생성 (startDate부터 endDate까지)
        LocalDate start = plan.getStartDate();
        LocalDate end = plan.getEndDate();
        int dayNumber = 1;

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            PlanDay planDay = PlanDay.builder()
                    .planId(plan.getId())
                    .day(dayNumber++)
                    .date(String.valueOf(date))
                    .build();
            planDayMapper.insert(planDay);
        }

        log.info("Created {} PlanDays for Plan ID: {}", dayNumber - 1, plan.getId());

        CreatePlanResponseDto responseDto = CreatePlanResponseDto.builder()
                .planId(plan.getId())
                .code(plan.getCode())
                .build();

        return responseDto;
    }

    private String generateCode() {
        String code = UUID.randomUUID().toString().substring(0, 8);
        return code;
    }

    @Transactional
    public void updatePlan(String title, String startDate, String endDate, Integer budget, Long planId, Long userId) {
        // 리더 맞는지 확인
        Plan plan = planMapper.findByPlanId(planId);
        if (!plan.getLeaderId().equals(userId)) {
            throw new PlanHandler(ErrorStatus._FORBIDDEN, "리더만 방을 수정할 수 있습니다.");
        }

        planMapper.update(title, LocalDate.parse(startDate), LocalDate.parse(endDate), budget, planId);

        // 수정 알림 메시지 전송
        chatService.sendMessage(userId,
                ChatMessagePub.builder()
                        .planId(planId)
                        .type(ChatType.PLAN_UPDATE)
                        .build());
    }

    @Transactional
    public JoinPlanResponseDto joinPlan(String code, Long userId) {
        // todo - 리더 본인은 참여불가
        Plan plan = planMapper.findByCode(code);
        PlanParticipant userPlan = PlanParticipant.builder()
                .userId(userId)
                .planId(plan.getId())
                .build();

        planParticipantMapper.insert(userPlan);

        JoinPlanResponseDto responseDto = JoinPlanResponseDto.builder()
                .planId(plan.getId())
                .build();

        // 입장 메시지 전송
        chatService.sendMessage(userId,
                ChatMessagePub.builder()
                        .planId(plan.getId())
                        .type(ChatType.ENTER)
                        .build());

        return responseDto;
    }

    @Transactional
    public void deletePlan(Long planId, Long userId) {
        // 리더 맞는지 확인
        Plan plan = planMapper.findByPlanId(planId);
        if (!plan.getLeaderId().equals(userId)) {
            throw new PlanHandler(ErrorStatus._FORBIDDEN, "리더만 방을 삭제할 수 있습니다.");
        }

        // 삭제 알림 메시지 전송
        chatService.sendMessage(userId,
                ChatMessagePub.builder()
                        .planId(planId)
                        .type(ChatType.PLAN_DELETE)
                        .build());

        // MongoDB 채팅 메시지 삭제
        chatService.deleteChatsByPlanId(planId);

        // 소프트 딜리트
        planParticipantMapper.delete(planId);
        planMapper.delete(planId);
    }

    @Transactional
    @RequirePlanMembership
    public void leavePlan(Long planId, Long userId) {
        Plan plan = planMapper.findByPlanId(planId);

        if (plan.getLeaderId().equals(userId)) {
            throw new PlanHandler(ErrorStatus._FORBIDDEN, "리더는 방을 나갈 수 없습니다. (방 삭제 권장)");
        }

        // 소딜 - 특정 사용자만 삭제
        planParticipantMapper.deleteByPlanIdAndUserId(planId, userId);

        // 퇴장 메시지 전송
        chatService.sendMessage(userId,
                ChatMessagePub.builder()
                        .planId(planId)
                        .type(ChatType.LEAVE)
                        .build());
    }

    public SliceResponse<PlanDto, Long> getPlans(Long userId, Long cursorId, int size) {
        // size + 1개를 조회하여 hasNext 판단
        List<Plan> plans = planMapper.findByUserIdWithCursor(userId, cursorId, size + 1);

        // hasNext 판단
        boolean hasNext = plans.size() > size;

        // size만큼만 자르기
        if (hasNext) {
            plans = plans.subList(0, size);
        }

        // DTO 변환
        List<PlanDto> planDtos = plans.stream()
                .map(plan -> {
                    int participantCount = planMapper.getPlanParticipantCount(plan.getId());
                    return PlanDto.builder()
                            .planId(plan.getId())
                            .leaderId(plan.getLeaderId())
                            .num(participantCount)
                            .title(plan.getTitle())
                            .startDate(plan.getStartDate().toString())
                            .endDate(plan.getEndDate().toString())
                            .build();
                })
                .toList();

        // nextCursorId 계산
        Long nextCursorId = null;
        if (!planDtos.isEmpty()) {
            nextCursorId = planDtos.get(planDtos.size() - 1).planId();
        }

        return SliceResponse.<PlanDto, Long>builder()
                .content(planDtos)
                .hasNext(hasNext)
                .nextCursor(nextCursorId)
                .build();
    }

    @RequirePlanMembership
    public GetPlanResponseDto getPlan(Long planId, Long userId) {
        Plan plan = planMapper.findByPlanId(planId);

        // user랑 조인해서 user 객체들 가져오기
        List<User> users = planParticipantMapper.findUsersByPlanId(planId);

        List<GetPlanResponseDto.UserDto> userDtos = users.stream()
                .map(user -> GetPlanResponseDto.UserDto.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .isDeleted(user.getDeletedAt() != null)
                        .build())
                .toList();

        // PlanDay 및 Attraction 목록 조회
        List<PlanDay> planDays = planDayMapper.findByPlanId(planId);

        // N+1 문제 해결: 모든 PlanDay ID 수집
        List<Long> planDayIds = planDays.stream()
                .map(PlanDay::getId)
                .toList();

        // 한 번의 쿼리로 모든 관광지 조회 후 Map으로 그룹핑
        Map<Long, List<PlanDayAttractionResponseDto>> attractionMap = planDayAttractionService
                .getAttractionsMapByPlanDayIds(planDayIds);

        List<GetPlanResponseDto.PlanDayWithAttractions> dayDtos = planDays.stream()
                .map(day -> GetPlanResponseDto.PlanDayWithAttractions.builder()
                        .planDayId(day.getId())
                        .day(day.getDay())
                        .date(day.getDate())
                        .attractions(attractionMap.getOrDefault(day.getId(), Collections.emptyList()))
                        .build())
                .toList();

        return GetPlanResponseDto.builder()
                .planId(plan.getId())
                .leaderId(plan.getLeaderId())
                .code(plan.getCode())
                .title(plan.getTitle())
                .startDate(plan.getStartDate().toString())
                .endDate(plan.getEndDate().toString())
                .budget(plan.getBudget())
                .users(userDtos)
                .days(dayDtos)
                .build();
    }
}
