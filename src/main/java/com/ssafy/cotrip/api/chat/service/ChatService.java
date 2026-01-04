package com.ssafy.cotrip.api.chat.service;

import com.ssafy.cotrip.api.chat.document.ChatDocument;
import com.ssafy.cotrip.api.chat.dto.request.ChatMessagePub;
import com.ssafy.cotrip.api.chat.dto.response.ChatCursor;
import com.ssafy.cotrip.api.chat.dto.response.ChatHistoryDto;
import com.ssafy.cotrip.api.chat.dto.response.ChatMessageSub;
import com.ssafy.cotrip.api.chat.mongo.ChatRepository;
import com.ssafy.cotrip.api.user.repository.UserMapper;
import com.ssafy.cotrip.apiPayload.code.status.ErrorStatus;
import com.ssafy.cotrip.apiPayload.exception.handler.UserHandler;
import com.ssafy.cotrip.domain.User;
import com.ssafy.cotrip.global.util.SliceResponse;
import com.ssafy.cotrip.global.util.SliceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

        private final SimpMessagingTemplate template;
        private final ChatRepository chatRepository;
        private final UserMapper userMapper;
        private final SliceService sliceService;

        public void sendMessage(User user, ChatMessagePub request) {
                // document 생성
                ChatDocument chatDocument = ChatDocument.builder()
                                .planId(request.planId())
                                .userId(user.getId())
                                .sender(user.getNickname())
                                .content(request.content())
                                .type(request.type())
                                .timestamp(LocalDateTime.now())
                                .build();

                // MongoDB 저장
                ChatDocument saved = chatRepository.save(chatDocument);

                // 브로드캐스트할 응답 메시지 생성
                ChatMessageSub response = ChatMessageSub.builder()
                                .id(saved.getId())
                                .planId(saved.getPlanId())
                                .userId(saved.getUserId())
                                .sender(saved.getSender())
                                .content(saved.getContent())
                                .type(saved.getType())
                                .timestamp(saved.getTimestamp())
                                .build();

                // STOMP 브로드캐스트
                template.convertAndSend(
                                "/sub/chat/" + saved.getPlanId(),
                                response);
        }

        public void sendMessage(Long userId, ChatMessagePub request) {
                User user = userMapper.findById(userId);

                if (user == null) {
                        throw new UserHandler(ErrorStatus.MEMBER_NO_EXIST);
                }

                sendMessage(user, request);
        }

        public SliceResponse<ChatHistoryDto, ChatCursor> getChatHistory(
                        Long planId,
                        String cursorId,
                        LocalDateTime cursorTimestamp,
                        int size) {
                List<ChatDocument> docs = chatRepository.findChatHistory(planId, cursorId, cursorTimestamp, size + 1);

                List<ChatHistoryDto> dtos = docs.stream()
                                .map(ChatHistoryDto::from)
                                .toList();

                return sliceService.toSliceResponse(
                                dtos,
                                size,
                                ch -> new ChatCursor(ch.id(), ch.timestamp()));
        }

        /**
         * AI 분석용 채팅 조회 (최신 N개, 커서 없이)
         */
        public List<ChatHistoryDto> getChatHistoryForAi(Long planId, int size) {
                // 기존 repository 메서드 재사용 (cursorId, cursorTimestamp null)
                List<ChatDocument> docs = chatRepository.findChatHistory(planId, null, null, size);

                return docs.stream()
                                .map(ChatHistoryDto::from)
                                .toList();
        }

        /**
         * Plan 삭제 시 해당 Plan의 모든 채팅 메시지 삭제
         */
        public void deleteChatsByPlanId(Long planId) {
                chatRepository.deleteByPlanId(planId);
        }
}
