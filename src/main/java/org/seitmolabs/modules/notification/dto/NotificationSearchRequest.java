package org.seitmolabs.modules.notification.dto;

import java.time.Instant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.seitmolabs.modules.notification.domain.NotificationAction;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

public record NotificationSearchRequest(
        @Positive Long processingRuleId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant updatedTo,
        NotificationStatus status,
        @Positive Long categoryId,
        String tag,
        @Size(max = 200) String q,
        NotificationAction.Type actionType,
        String actorId,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        Order sort
) {
    public enum Order {
        UPDATED_AT_DESC("updatedAt", Sort.Direction.DESC),
        UPDATED_AT_ASC("updatedAt", Sort.Direction.ASC),
        CREATED_AT_DESC("createdAt", Sort.Direction.DESC),
        CREATED_AT_ASC("createdAt", Sort.Direction.ASC);

        private final String field;
        private final Sort.Direction direction;

        Order(String field, Sort.Direction direction) {
            this.field = field;
            this.direction = direction;
        }

        public Sort toSort() {
            return Sort.by(direction, field, "id");
        }
    }
}
