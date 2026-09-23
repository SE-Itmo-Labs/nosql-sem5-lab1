package org.seitmolabs.repository;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Block;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * BlockRepository — временные блокировки ресурсов в Redis.
 * <p>
 * Назначение: сценарий TTL.
 * Ключ {@code block:{resourceId}} кладётся с {@code EX ttlSeconds} и исчезает сам
 * по истечении TTL — отдельный «джоб» для очистки не нужен.
 */
@Repository
@RequiredArgsConstructor
public class BlockRepository {

    private static final String KEY_PREFIX = "block:";

    private final StringRedisTemplate redis;

    /** Установить блокировку с TTL (SET block:{resourceId} ... EX ttl). */
    public void save(Block block) {
        throw new UnsupportedOperationException("TODO: ObjectMapper → JSON, SET KEY_PREFIX + resourceId EX ttlSeconds");
    }

    /** Активна ли блокировка (пусто, если TTL истёк и ключ удалён). */
    public Optional<Block> findActive(String resourceId) {
        throw new UnsupportedOperationException("TODO: redis.opsForValue().get(KEY_PREFIX + resourceId) → JSON → Block");
    }
}