package org.seitmolabs.demo;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.notification.domain.ProcessingRule;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEventType;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Order(30)
@RequiredArgsConstructor
class ProcessingRuleDemo implements DemoScenario {
    static final long CONFIRMED_RULE_ID = 7L;
    static final long CANCELLED_RULE_ID = 8L;
    static final long HELD_RULE_ID = 9L;

    private final MongoTemplate mongoTemplate;

    @Override
    public void run() {
        createRuleIfMissing(CONFIRMED_RULE_ID, "Подтверждение бронирования",
                OutboxEventType.ORDER_CONFIRMED, List.of("booking", "confirmed"));
        createRuleIfMissing(CANCELLED_RULE_ID, "Отмена бронирования",
                OutboxEventType.ORDER_CANCELLED, List.of("booking", "cancelled"));
        createRuleIfMissing(HELD_RULE_ID, "Удержание места",
                OutboxEventType.SEAT_HOLD_CREATED, List.of("booking", "seat-hold"));
    }

    private void createRuleIfMissing(long id, String name, OutboxEventType eventType, List<String> tags) {
        // Keep the demo rule available between restarts without overwriting existing data.
        mongoTemplate.upsert(Query.query(Criteria.where("id").is(id)), new Update()
                .setOnInsert("name", name)
                .setOnInsert("eventType", eventType)
                .setOnInsert("tagsToAdd", tags), ProcessingRule.class);
    }
}
