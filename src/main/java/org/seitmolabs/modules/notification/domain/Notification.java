package org.seitmolabs.modules.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    public Notification(String userId, String title, String text, Long categoryId,
            NotificationStatus status, Instant createdAt) {
        setUserId(userId);
        setTitle(title);
        setText(text);
        setCategoryId(categoryId);
        setStatus(status);
        setCreatedAt(createdAt);
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

    @Id
    private Long id;

    private String userId;

    private String title;

    private String text;

    private Long categoryId;

    public NotificationStatus status;

    private Instant createdAt;

    @Indexed(
            unique = true,
            partialFilter = "{ 'sourceEventId': { '$type': 'string' } }"
    )
    private String sourceEventId;
}
