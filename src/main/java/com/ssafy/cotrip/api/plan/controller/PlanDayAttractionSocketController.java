package com.ssafy.cotrip.api.plan.controller;

import com.ssafy.cotrip.api.plan.dto.request.AddAttractionRequestDto;
import com.ssafy.cotrip.api.plan.dto.request.UpdateAttractionContentRequestDto;
import com.ssafy.cotrip.api.plan.dto.request.UpdateAttractionTimeRequestDto;
import com.ssafy.cotrip.api.plan.dto.response.AttractionEventResponseDto;
import com.ssafy.cotrip.api.plan.dto.response.PlanDayAttractionResponseDto;
import com.ssafy.cotrip.api.plan.service.EditLockService;
import com.ssafy.cotrip.api.plan.service.PlanDayAttractionService;
import com.ssafy.cotrip.security.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PlanDayAttractionSocketController {

    private final PlanDayAttractionService planDayAttractionService;
    private final EditLockService editLockService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/plan/{planId}/attraction/add")
    public void addAttraction(@DestinationVariable Long planId,
            @Valid @Payload AddAttractionRequestDto request,
            SimpMessageHeaderAccessor headers) {

        PlanDayAttractionResponseDto result = planDayAttractionService.addAttraction(request);

        AttractionEventResponseDto event = AttractionEventResponseDto.builder()
                .type("ADD")
                .data(result)
                .build();

        messagingTemplate.convertAndSend("/sub/plan/" + planId + "/attraction", event);
    }

    @MessageMapping("/plan/{planId}/attraction/update-time")
    public void updateTime(@DestinationVariable Long planId,
            @Valid @Payload UpdateAttractionTimeRequestDto request,
            SimpMessageHeaderAccessor headers) {

        PlanDayAttractionResponseDto result = planDayAttractionService.updateTime(request);

        AttractionEventResponseDto event = AttractionEventResponseDto.builder()
                .type("UPDATE_TIME")
                .data(result)
                .build();

        messagingTemplate.convertAndSend("/sub/plan/" + planId + "/attraction", event);
    }

    // 편집 시작 (Focus)
    @MessageMapping("/plan/{planId}/attraction/{attractionId}/focus")
    public void focus(@DestinationVariable Long planId,
            @DestinationVariable Long attractionId,
            SimpMessageHeaderAccessor headers) {
        CustomUserDetails user = getUserFromHeaders(headers);

        editLockService.startEdit(
                attractionId,
                user.getUser().getId(),
                user.getUser().getNickname(),
                planId);
    }

    // 타이핑 중 (실시간 전송 + TTL 갱신)
    @MessageMapping("/plan/{planId}/attraction/{attractionId}/typing")
    public void typing(@DestinationVariable Long planId,
            @DestinationVariable Long attractionId,
            String content,
            SimpMessageHeaderAccessor headers) {
        CustomUserDetails user = getUserFromHeaders(headers);

        editLockService.typing(
                attractionId,
                user.getUser().getId(),
                content,
                planId);
    }

    // 편집 완료 (Blur + 저장)
    @MessageMapping("/plan/{planId}/attraction/update-content")
    public void updateContent(@DestinationVariable Long planId,
            @Valid @Payload UpdateAttractionContentRequestDto request,
            SimpMessageHeaderAccessor headers) {
        CustomUserDetails user = getUserFromHeaders(headers);

        // DB 저장
        PlanDayAttractionResponseDto result = planDayAttractionService.updateContent(request);

        // 편집 종료 (락 해제)
        editLockService.finishEdit(request.id(), user.getUser().getId(), planId);

        AttractionEventResponseDto event = AttractionEventResponseDto.builder()
                .type("UPDATE_CONTENT")
                .data(result)
                .build();

        messagingTemplate.convertAndSend("/sub/plan/" + planId + "/attraction", event);
    }

    // 장소 변경
    @MessageMapping("/plan/{planId}/attraction/{attractionId}/change-location")
    public void changeLocation(@DestinationVariable Long planId,
            @DestinationVariable Long attractionId,
            @RequestBody Map<String, Object> payload,
            SimpMessageHeaderAccessor headers) {

        // payload에서 카카오 장소 데이터 추출
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoPlaceData = (Map<String, Object>) payload.get("kakaoPlaceData");

        // 장소 변경 처리 (newAttractionId는 null로 전달, kakaoPlaceData에서 찾거나 생성)
        PlanDayAttractionResponseDto result = planDayAttractionService.changeLocation(
                attractionId,
                null, // kakaoPlaceData를 사용하므로 null
                kakaoPlaceData);

        log.info("장소 변경 완료 - 새 attractionId: {}, 새 장소명: {}",
                result.attractionId(), result.attractionTitle());

        AttractionEventResponseDto event = AttractionEventResponseDto.builder()
                .type("CHANGE_LOCATION")
                .data(result)
                .build();

        messagingTemplate.convertAndSend("/sub/plan/" + planId + "/attraction", event);
        log.info("장소 변경 브로드캐스트 완료");
    }

    @MessageMapping("/plan/{planId}/attraction/delete")
    public void deleteAttraction(@DestinationVariable Long planId,
            @Payload Long attractionId,
            SimpMessageHeaderAccessor headers) {

        planDayAttractionService.deleteAttraction(attractionId);

        AttractionEventResponseDto event = AttractionEventResponseDto.builder()
                .type("DELETE")
                .deletedId(attractionId)
                .build();

        messagingTemplate.convertAndSend("/sub/plan/" + planId + "/attraction", event);
    }

    @MessageExceptionHandler
    public void handleException(Exception e, SimpMessageHeaderAccessor headers) {
        messagingTemplate.convertAndSendToUser(headers.getUser().getName(), "/queue/errors", e.getMessage());
    }

    private CustomUserDetails getUserFromHeaders(SimpMessageHeaderAccessor headers) {
        Authentication authentication = (Authentication) headers.getUser();
        if (authentication == null) {
            throw new AccessDeniedException("Unauthorized");
        }

        CustomUserDetails cud = (CustomUserDetails) authentication.getPrincipal();
        if (cud == null || cud.getUser() == null) {
            throw new AccessDeniedException("Invalid user");
        }

        return cud;
    }
}
