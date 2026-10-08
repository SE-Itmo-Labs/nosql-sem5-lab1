package org.seitmolabs.modules.notification.repositories;

import lombok.RequiredArgsConstructor;

import org.seitmolabs.modules.notification.domain.Notification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {

    private static final String KEY_PREFIX = "notification:";
    private static final String USER_INDEX_PREFIX = "notification:user:";
    private static final String SEQ_KEY = "notification:seq";
    private static final DefaultRedisScript<Long> SAVE_SCRIPT = new DefaultRedisScript<>(
            "redis.call('SET', KEYS[1], ARGV[1]); " +
                    "redis.call('ZADD', KEYS[2], ARGV[2], ARGV[3]); " +
                    "return 1;",
            Long.class
    );

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    public Long nextId() {
        return redis.opsForValue().increment(SEQ_KEY);
    }

    public void save(Notification notification) {
        try {
            String jsonNotif = jsonMapper.writeValueAsString(notification);

            redis.execute(
                    SAVE_SCRIPT,
                    List.of(
                            KEY_PREFIX + notification.getId(),
                            USER_INDEX_PREFIX + notification.getUserId()
                    ),
                    jsonNotif,
                    String.valueOf(notification.getCreatedAt().toEpochMilli()),
                    String.valueOf(notification.getId())
            );
        } catch (JacksonException e) {
            throw new IllegalStateException("Не удалось сохранить уведомление", e);
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

    public List<Long> findIdsByUserId(String userId) {
        Set<String> ids = redis.opsForZSet()
                .reverseRange(USER_INDEX_PREFIX + userId, 0, -1);

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
                .map(Long::valueOf)
                .toList();
    }
}
