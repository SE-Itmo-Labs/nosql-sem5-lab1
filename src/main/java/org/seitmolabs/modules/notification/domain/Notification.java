package org.seitmolabs.modules.notification.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEventType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = Notification.COLLECTION_NAME)
public class Notification {

    public static final String COLLECTION_NAME = "notifications";

    public Notification(String userId, String title, String text, Long categoryId,
            NotificationStatus status, Instant createdAt) {
        this(userId, title, text, categoryId, status, createdAt, null);
    }

    public Notification(String userId, String title, String text, Long categoryId,
                        NotificationStatus status, Instant createdAt, String sourceEventId) {
        setUserId(userId);
        setTitle(title);
        setText(text);
        setCategoryId(categoryId);
        setStatus(status);
        setCreatedAt(createdAt);
        setSourceEventId(sourceEventId);
    }

    public Notification(Long id, String userId, String title, String text, Long categoryId,
                        NotificationStatus status, Instant createdAt, String sourceEventId) {
        this(userId, title, text, categoryId, status, createdAt, sourceEventId);
        setId(id);
    }

    @Id
    private Long id;

    private String userId;

    private String title;

    private String text;

    private Long categoryId;

    public NotificationStatus status;

    private Instant createdAt;

    private String sourceEventId;

    @JsonIgnore
    private OutboxEventType sourceEventType;

    @JsonIgnore
    private Long processingRuleId;

    @JsonIgnore
    private List<String> tags = List.of();

    // The journal is persisted with the notification and contains internal actor information.
    @JsonIgnore
    private List<NotificationAction> actionLog = List.of();
}
