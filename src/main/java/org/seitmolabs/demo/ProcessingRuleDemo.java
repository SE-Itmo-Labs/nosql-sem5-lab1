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
    private final MongoTemplate mongoTemplate;

    @Override
    public void run() {
        // Keep the demo rule available between restarts without overwriting existing data.
        mongoTemplate.upsert(Query.query(Criteria.where("id").is(7L)), new Update()
                .setOnInsert("name", "Подтверждение бронирования")
                .setOnInsert("eventType", OutboxEventType.ORDER_CONFIRMED)
                .setOnInsert("tagsToAdd", List.of("booking", "confirmed")), ProcessingRule.class);
    }
}
