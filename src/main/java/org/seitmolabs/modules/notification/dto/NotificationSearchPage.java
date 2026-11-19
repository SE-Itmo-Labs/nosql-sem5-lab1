package org.seitmolabs.modules.notification.dto;

import java.time.Instant;
import java.util.List;

import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationStatus;

public record NotificationSearchPage(List<Item> items, int page, int size, long totalElements, long totalPages) {

    public record Item(
            Long id, String userId, String title, String text, Long categoryId, NotificationStatus status,
            Instant createdAt, Instant updatedAt, String sourceEventId, Long processingRuleId, List<String> tags
    ) {
        public static Item from(Notification notification) {
            return new Item(notification.getId(), notification.getUserId(), notification.getTitle(),
                    notification.getText(), notification.getCategoryId(), notification.getStatus(),
                    notification.getCreatedAt(), notification.getUpdatedAt(), notification.getSourceEventId(),
                    notification.getProcessingRuleId(), notification.getTags());
        }
    }
}
