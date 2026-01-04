package com.ssafy.cotrip.api.chat.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_messages")
public class ChatDocument {

    @Id
    private String id;

    private Long planId;
    private Long userId;
    private String sender;
    private String content;
    private ChatType type;
    private LocalDateTime timestamp;

}
