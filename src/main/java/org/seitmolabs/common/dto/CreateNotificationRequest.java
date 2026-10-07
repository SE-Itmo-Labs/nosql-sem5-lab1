package org.seitmolabs.common.dto;

public record CreateNotificationRequest(
        String userId,
        String title,
        String text,
        String categoryId
) {
}