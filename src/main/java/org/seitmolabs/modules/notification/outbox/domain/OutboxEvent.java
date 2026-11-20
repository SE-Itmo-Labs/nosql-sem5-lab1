package org.seitmolabs.modules.notification.outbox.domain;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "outbox_events")
public class OutboxEvent {
    @Id
    private UUID id;

    private OutboxEventType eventType;

    private String aggregateId;

    private String username;

    private String title;

    private String message;

    private String categoryName;

    private Instant createdAt;

    private Instant deliveredAt;

    private Instant lastAttemptAt;

    private int attempts;

    private String lastError;
}
