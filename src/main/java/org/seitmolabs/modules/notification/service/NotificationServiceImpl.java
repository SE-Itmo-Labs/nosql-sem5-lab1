package org.seitmolabs.modules.notification.service;

import java.util.List;
import java.util.Optional;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.factory.NotificationFactory;
import org.seitmolabs.modules.notification.repositories.NotificationRepository;
import org.seitmolabs.modules.notification.outbox.domain.OutboxEvent;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationFactory notificationFactory;
    private final CategoryDbRepository categoryRepository;

    @Override
    public Notification create(CreateNotificationRequest request) {
        if (userRepository.findByUsername(request.userId()).isEmpty()) {
            throw new EntityNotFoundException("Пользователь не найден: " + request.userId());
        }

        Notification notification = notificationFactory.createSent(
                notificationRepository.nextId(),
                request
        );

        notificationRepository.save(notification);
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
        return notificationRepository.findIdsByUserId(userId).stream()
                .map(notificationRepository::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    @Override
    public Notification markRead(Long id, String userId) {
        Notification notification = getByIdForUser(id, userId);
        return notificationRepository.markRead(notification);
    }

    @Override
    public void delete(Long id, String userId) {
        notificationRepository.delete(getByIdForUser(id, userId));
    }

    @Override
    public void deliver(OutboxEvent event) {
        Long categoryId = categoryRepository.findByName(event.getCategoryName())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Категория уведомления не найдена: " + event.getCategoryName()))
                .getId();
        Notification notification = notificationFactory.createSystem(
                notificationRepository.nextId(), event, categoryId);
        notificationRepository.saveSystem(notification);
    }
}
