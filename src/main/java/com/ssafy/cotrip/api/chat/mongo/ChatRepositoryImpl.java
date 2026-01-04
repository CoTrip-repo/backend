package com.ssafy.cotrip.api.chat.mongo;

import com.ssafy.cotrip.api.chat.document.ChatDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<ChatDocument> findChatHistory(Long planId, String cursorId, LocalDateTime cursorTimestamp, int limit) {

        Criteria criteria = Criteria.where("planId").is(planId);

        // cursor가 있을 때
        if (cursorId != null && cursorTimestamp != null) {
            Criteria olderThanCursor = new Criteria().orOperator(
                    Criteria.where("timestamp").lt(cursorTimestamp),
                    new Criteria().andOperator(
                            Criteria.where("timestamp").is(cursorTimestamp),
                            Criteria.where("_id").lt(cursorId)
                    )
            );
            criteria = new Criteria().andOperator(criteria, olderThanCursor);
        }

        Query query = new Query(criteria)
                .with(Sort.by(Sort.Order.desc("timestamp"), Sort.Order.desc("_id")))
                .limit(limit);

        return mongoTemplate.find(query, ChatDocument.class);
    }
}

