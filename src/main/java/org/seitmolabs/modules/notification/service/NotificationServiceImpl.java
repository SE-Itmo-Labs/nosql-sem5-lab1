package org.seitmolabs.modules.notification.service;

import java.util.List;
import java.util.Optional;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.factory.NotificationFactory;
import org.seitmolabs.modules.notification.repositories.MongoNotificationRepository;
import org.seitmolabs.modules.notification.repositories.NotificationRepository;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final MongoNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationFactory notificationFactory;
    private final CategoryDbRepository categoryRepository;

    @Override
    public Notification create(CreateNotificationRequest request) {
        if (userRepository.findByUsername(request.userId()).isEmpty()) {
            throw new EntityNotFoundException("Пользователь не найден: " + request.userId());
        }

//        Notification notification = notificationFactory.createSent(
//                notificationRepository.nextId(),
//                request
//        );

        Notification notification = notificationFactory.createSent(request);

        notificationRepository.insert(notification);
        return notification;
    }

    @Override
    public Notification getById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Уведомление не найдено: " + id));
    }

    @Override
    public Notification getByIdForUser(Long id, String userId) {
        Notification notification = getById(id);
        if (!notification.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Уведомление не найдено: " + id);
        }
        return notification;
    }

    @Override
    public List<Notification> getByUserId(String userId) {

//        return notificationRepository.findIdsByUserId(userId).stream()
//                .map(notificationRepository::findById)
//                .flatMap(Optional::stream)
//                .toList();

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Notification markRead(Long id, String userId) {
        Notification notification = getByIdForUser(id, userId);

        notification.setStatus(NotificationStatus.READ);

        return notificationRepository.save(notification);
    }

    @Override
    public void delete(Long id, String userId) {
        notificationRepository.delete(getByIdForUser(id, userId));
    }

    @Override
    public void deliver(OutboxEvent event) {
        Long categoryId = categoryRepository.findBySystemCode(Category.BOOKING_CODE)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Системная категория уведомлений не найдена: " + Category.BOOKING_CODE))
                .getId();
        Notification notification = notificationFactory.createSystem(
                event, categoryId);
        notificationRepository.saveSystem(notification);
    }
}
