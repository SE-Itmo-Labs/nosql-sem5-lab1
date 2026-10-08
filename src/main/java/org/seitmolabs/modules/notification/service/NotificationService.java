package org.seitmolabs.modules.notification.service;

import java.util.List;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.notification.domain.Notification;

public interface NotificationService {

    Notification create(CreateNotificationRequest request);

    Notification getById(Long id);

    List<Notification> getByUserId(String userId);
}
