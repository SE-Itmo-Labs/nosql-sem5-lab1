package org.seitmolabs.modules.notification.repositories;


import org.seitmolabs.modules.notification.domain.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoNotificationRepository extends MongoRepository<Notification, Long> {

    // вывод уведомлений по id с самых новых
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

}
