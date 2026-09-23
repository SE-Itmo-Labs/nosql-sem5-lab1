package org.seitmolabs.service;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Notification;
import org.seitmolabs.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * NotificationService — обязательный сценарий «отправка уведомлений».
 * <p>
 * Назначение: бизнес-логика создания и выборки уведомлений поверх Redis:
 * INCR id → сохранить JSON → добавить id в индекс клиента → вернуть сохранённое.
 * Как именно устроены ключи — знает только repository, сервис оперирует моделями.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    /** Создать и «отправить» уведомление клиенту. */
    public Notification create(String userId, String title, String text, String categoryId) {
        throw new UnsupportedOperationException("TODO: repository.nextId() → собрать Notification → save(...) → addToUserIndex(...)");
    }

    /** Уведомление по id (пусто, если такого нет). */
    public Optional<Notification> findById(Long id) {
        throw new UnsupportedOperationException("TODO: repository.findById(id)");
    }

    /** Все уведомления клиента в порядке создания. */
    public List<Notification> listByUserId(String userId) {
        throw new UnsupportedOperationException("TODO: repository.findIdsByUserId(userId) → findById(...) для каждого id");
    }
}