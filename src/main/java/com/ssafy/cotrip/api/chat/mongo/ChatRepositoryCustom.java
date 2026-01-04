package com.ssafy.cotrip.api.chat.mongo;

import com.ssafy.cotrip.api.chat.document.ChatDocument;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepositoryCustom {

    List<ChatDocument> findChatHistory(
            Long planId,
            String cursorId,                 // nullable
            LocalDateTime cursorTimestamp,   // nullable
            int limit                        // size + 1
    );

}

