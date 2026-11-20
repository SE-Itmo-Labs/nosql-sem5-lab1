package org.seitmolabs.modules.notification.domain;

import java.time.Instant;
import java.util.List;

import org.seitmolabs.modules.notification.outbox.domain.OutboxEventType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "processing_rules")
public record ProcessingRule(
        @Id Long id,
        String name,
        OutboxEventType eventType,
        List<String> tagsToAdd,
        Long appliedCount,
        Instant lastAppliedAt
) {
}
