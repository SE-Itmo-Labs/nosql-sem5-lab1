package org.seitmolabs.service;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Notification;
import org.seitmolabs.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    // TODO: После подключения БД пользователей проверять получателя перед созданием уведомления.
    public Notification create(String userId, String title, String text, String categoryId) {
        Long id = repository.nextId();
        Instant createdAt = Instant.now();

        Notification notification = new Notification(
                id,
                userId,
                title,
                text,
                categoryId,
                createdAt
        );

        repository.save(notification);
        repository.addToUserNotifSet(userId, id, createdAt);

        return notification;
    }

    public Optional<Notification> findById(Long id) {
        return repository.findById(id);
    }

    /** Возвращает уведомления от старых к новым. */
    public List<Notification> getNotifListByUserId(String userId) {
        return repository.findIdsByUserId(userId).stream()
                .map(repository::findById)
                .flatMap(Optional::stream)
                .toList();
    }
}