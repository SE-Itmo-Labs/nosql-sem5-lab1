package org.seitmolabs.modules.notification.domain;

import java.time.Instant;

public record NotificationAction(Type type, String actorId, Instant at, Long processingRuleId) {

    public enum Type {
        CREATED,
        READ,
        RULE_APPLIED
    }
}
