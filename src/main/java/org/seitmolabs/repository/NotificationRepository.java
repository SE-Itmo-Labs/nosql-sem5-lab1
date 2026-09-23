package org.seitmolabs.repository;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Notification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * NotificationRepository — доступ к уведомлениям в Redis.
 * <p>
 * Назначение: собрать схему ключей в одном месте и скрыть её от сервисного слоя.
 * Ключи:
 * <ul>
 *   <li>{@code notification:{id}} — JSON уведомления;</li>
 *   <li>{@code notification:user:{userId}} — sorted set id уведомлений клиента (score = id);</li>
 *   <li>{@code notification:seq} — инкрементный счётчик id (INCR).</li>
 * </ul>
 * Реализация через {@link StringRedisTemplate}: JSON-сериализация моделей
 * добавляется вместе с реализацией методов.
 */
@Repository
@RequiredArgsConstructor
public class NotificationRepository {

    private static final String KEY_PREFIX = "notification:";
    private static final String USER_INDEX_PREFIX = "notification:user:";
    private static final String SEQ_KEY = "notification:seq";

    private final StringRedisTemplate redis;

    /** Следующий id уведомления — атомарный INCR в Redis. */
    public Long nextId() {
        throw new UnsupportedOperationException("TODO: redis.opsForValue().increment(SEQ_KEY)");
    }

    /** Сохранить уведомление как JSON-строку по ключу notification:{id}. */
    public void save(Notification notification) {
        throw new UnsupportedOperationException("TODO: ObjectMapper → JSON, redis.opsForValue().set(KEY_PREFIX + id, json)");
    }

    /** Прочитать уведомление по id (пусто, если такой ключ отсутствует). */
    public Optional<Notification> findById(Long id) {
        throw new UnsupportedOperationException("TODO: redis.opsForValue().get(KEY_PREFIX + id) → JSON → Notification");
    }

    /** Добавить id уведомления в sorted set клиента (score = id — порядок создания). */
    public void addToUserIndex(String userId, Long notificationId) {
        throw new UnsupportedOperationException("TODO: redis.opsForZSet().add(USER_INDEX_PREFIX + userId, id, score)");
    }

    /** Список id уведомлений клиента по порядку создания. */
    public List<Long> findIdsByUserId(String userId) {
        throw new UnsupportedOperationException("TODO: redis.opsForZSet().range(USER_INDEX_PREFIX + userId, 0, -1)");
    }
}