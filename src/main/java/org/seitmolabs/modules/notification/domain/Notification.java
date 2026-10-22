package org.seitmolabs.modules.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    public Notification(Long id, String userId, String title, String text, Long categoryId,
            NotificationStatus status, Instant createdAt) {
        this(id, userId, title, text, categoryId, status, createdAt, null);
    }

    private Long id;

    private String userId;

    private String title;

    private String text;

    private Long categoryId;

    private NotificationStatus status;

    private Instant createdAt;

    private String sourceEventId;
}
