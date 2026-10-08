package org.seitmolabs.modules.notification.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.domain.NotificationStatus;
import org.seitmolabs.modules.notification.repositories.NotificationRepository;
import org.seitmolabs.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public Notification create(CreateNotificationRequest request) {
        if (userRepository.findByUsername(request.userId()).isEmpty()) {
            throw new EntityNotFoundException("Пользователь не найден: " + request.userId());
        }

        Notification notification = new Notification(
                notificationRepository.nextId(),
                request.userId(),
                request.title(),
                request.text(),
                request.categoryId(),
                NotificationStatus.SENT,
                Instant.now()
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
    public List<Notification> getByUserId(String userId) {
        return notificationRepository.findIdsByUserId(userId).stream()
                .map(notificationRepository::findById)
                .flatMap(Optional::stream)
                .toList();
    }
}
