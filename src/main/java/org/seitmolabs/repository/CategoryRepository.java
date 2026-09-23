package org.seitmolabs.repository;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Category;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * CategoryRepository — доступ к справочнику категорий в Redis.
 * <p>
 * Назначение: скрыть схему ключей от сервисного слоя.
 * Ключ: {@code category:{id}} — JSON категории.
 * TTL для кэша задаёт сервис (cache-aside), репозиторий сам его не трогает.
 */
@Repository
@RequiredArgsConstructor
public class CategoryRepository {

    private static final String KEY_PREFIX = "category:";

    private final StringRedisTemplate redis;

    /** Сохранить категорию (записать/обновить источник). */
    public void save(Category category) {
        throw new UnsupportedOperationException("TODO: ObjectMapper → JSON, redis.opsForValue().set(KEY_PREFIX + id, json)");
    }

    /** Прочитать категорию по id (пусто при промахе кэша). */
    public Optional<Category> findById(Long id) {
        throw new UnsupportedOperationException("TODO: redis.opsForValue().get(KEY_PREFIX + id) → JSON → Category");
    }

    /** Удалить категорию (инвалидация кэша при изменении справочника). */
    public void delete(Long id) {
        throw new UnsupportedOperationException("TODO: redis.delete(KEY_PREFIX + id)");
    }
}