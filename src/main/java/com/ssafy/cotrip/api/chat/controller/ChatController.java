package com.ssafy.cotrip.api.chat.controller;

import com.ssafy.cotrip.api.chat.dto.request.ChatMessagePub;
import com.ssafy.cotrip.api.chat.dto.response.ChatCursor;
import com.ssafy.cotrip.api.chat.dto.response.ChatHistoryDto;
import com.ssafy.cotrip.api.chat.service.ChatService;
import com.ssafy.cotrip.global.util.SliceResponse;
import com.ssafy.cotrip.security.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // 실시간 채팅 메시지 전송
    @MessageMapping("/chat/message")
    public void handleChatMessage(@Valid @Payload ChatMessagePub request,
                                  SimpMessageHeaderAccessor headers) {

        Principal principal = headers.getUser();
        if (!(principal instanceof Authentication authentication)) {
            throw new AccessDeniedException("Unauthorized");
        }

        CustomUserDetails cud = (CustomUserDetails) authentication.getPrincipal();
        chatService.sendMessage(cud.getUser(), request);
    }

    // 채팅 이력 조회 - 무한 스크롤
    @GetMapping("/api/v1/plans/{planId}/chat")
    @ResponseBody
    public SliceResponse<ChatHistoryDto, ChatCursor> getChatHistory(
            @PathVariable Long planId,
            @RequestParam(required = false) String cursorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTimestamp,
            @RequestParam(defaultValue = "30") Integer size
    ) {
        return chatService.getChatHistory(planId, cursorId, cursorTimestamp, size);
    }

}
