package org.seitmolabs.repository;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Notification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Схема хранения уведомлений в Redis:
 * <ul>
 *   <li>{@code notification:{id}} — JSON уведомления;</li>
 *   <li>{@code notification:user:{userId}} — sorted set id, score — время создания в миллисекундах Unix;</li>
 *   <li>{@code notification:seq} — счётчик id (INCR).</li>
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class NotificationRepository {

    private static final String KEY_PREFIX = "notification:";
    private static final String USER_INDEX_PREFIX = "notification:user:";
    private static final String SEQ_KEY = "notification:seq";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    public Long nextId() {
        return redis.opsForValue().increment(SEQ_KEY);
    }

    // TODO: Атомарно сохранять уведомление и добавлять его id в индекс пользователя.
    public void save(Notification notification) {
        try {
            String jsonNotif = jsonMapper.writeValueAsString(notification);
            redis.opsForValue().set(KEY_PREFIX + notification.getId(), jsonNotif);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize notification", e);
        }
    }

    public Optional<Notification> findById(Long id) {
        String jsonNotif = redis.opsForValue().get(KEY_PREFIX + id);

        if (jsonNotif == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    jsonMapper.readValue(jsonNotif, Notification.class)
            );
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Не удалось десериализовать уведомление с id " + id,
                    e
            );
        }
    }

    public void addToUserNotifSet(
            String userId,
            Long notificationId,
            Instant createdAt
    ) {
        redis.opsForZSet().add(
                USER_INDEX_PREFIX + userId,
                notificationId.toString(),
                createdAt.toEpochMilli()
        );
    }

    public List<Long> findIdsByUserId(String userId) {
        Set<String> ids = redis.opsForZSet()
                .range(USER_INDEX_PREFIX + userId, 0, -1);

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
                .map(Long::valueOf)
                .toList();
    }
}