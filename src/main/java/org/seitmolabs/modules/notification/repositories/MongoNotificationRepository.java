package org.seitmolabs.modules.notification.repositories;


import org.seitmolabs.modules.notification.domain.Notification;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoNotificationRepository extends MongoRepository<Notification, Long> {

    // вывод уведомлений по id с самых новых
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    boolean existsBySourceEventId(String sourceEventId);

    default void saveSystem(Notification notification) {
        String eventId = notification.getSourceEventId();

        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Need sourceId");
        }

        try {
            insert(notification);
        } catch (DuplicateKeyException e) {
            if (!existsBySourceEventId(eventId)) {
                throw e;
            }
        }
    }
}
