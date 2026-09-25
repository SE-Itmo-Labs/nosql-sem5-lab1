package org.seitmolabs.repository;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Category;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryCacheRepository {

    private static final String KEY_PREFIX = "category:";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    public void put(Category category, Duration ttl) {
        try {
            String jsonCategory = jsonMapper.writeValueAsString(category);

            redis.opsForValue().set(
                    KEY_PREFIX + category.getId(),
                    jsonCategory,
                    ttl
            );
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Не удалось сериализовать категорию с id " + category.getId(),
                    e
            );
        }
    }

    public Optional<Category> findById(Long id) {
        String jsonCategory = redis.opsForValue().get(KEY_PREFIX + id);

        if (jsonCategory == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(
                    jsonMapper.readValue(jsonCategory, Category.class)
            );
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Не удалось десериализовать категорию с id " + id,
                    e
            );
        }
    }

    public void delete(Long id) {
        redis.delete(KEY_PREFIX + id);
    }
}