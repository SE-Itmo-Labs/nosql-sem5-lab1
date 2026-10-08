package org.seitmolabs.modules.categories.repositories;

import lombok.RequiredArgsConstructor;

import org.seitmolabs.modules.categories.domain.Category;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class CategoryCacheRepository {

    private static final String KEY_PREFIX = "category:";
    private static final String LIST_KEY = "category:all";

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

    public void putAll(List<Category> categories, Duration ttl) {
        try {
            String jsonCategories = jsonMapper.writeValueAsString(categories);
            redis.opsForValue().set(LIST_KEY, jsonCategories, ttl);
        } catch (JacksonException e) {
            throw new IllegalStateException("Не удалось сохранить список категорий в кэш", e);
        }
    }

    public Optional<List<Category>> findAll() {
        String jsonCategories = redis.opsForValue().get(LIST_KEY);

        if (jsonCategories == null) {
            return Optional.empty();
        }

        try {
            Category[] categories = jsonMapper.readValue(jsonCategories, Category[].class);
            return Optional.of(Arrays.asList(categories));
        } catch (JacksonException e) {
            throw new IllegalStateException("Не удалось прочитать список категорий из кэша", e);
        }
    }

    public long getRemainingTtlSeconds(Long id) {
        Long ttl = redis.getExpire(KEY_PREFIX + id, TimeUnit.SECONDS);
        return ttl == null ? -2 : ttl;
    }

    public void delete(Long id) {
        redis.delete(KEY_PREFIX + id);
    }

    public void deleteList() {
        redis.delete(LIST_KEY);
    }
}
