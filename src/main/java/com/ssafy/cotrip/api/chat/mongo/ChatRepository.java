package com.ssafy.cotrip.api.chat.mongo;

import com.ssafy.cotrip.api.chat.document.ChatDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<ChatDocument, String>, ChatRepositoryCustom {
    // Plan 삭제 시 해당 Plan의 모든 채팅 메시지 삭제
    void deleteByPlanId(Long planId);
}
