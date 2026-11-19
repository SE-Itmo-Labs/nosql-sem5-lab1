package org.seitmolabs.modules.notification.service;

import java.util.List;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;

public interface NotificationService {

    Notification create(CreateNotificationRequest request, String actorId);

    Notification getById(Long id);

    Notification getByIdForUser(Long id, String userId);

    List<Notification> getByUserId(String userId);

    Notification markRead(Long id, String userId);

    Notification applyRule(Long id, Long ruleId, String actorId);

    void delete(Long id, String userId);

    void deliver(OutboxEvent event);
}
