package org.seitmolabs.service;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Category;
import org.seitmolabs.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * CategoryService — сценарий кэширования справочника категорий (cache-aside).
 * <p>
 * Назначение: чтение категорий через кэш Redis.
 * Алгоритм: пробуем кэш → при промахе читаем «источник» (БД) → кладём в Redis с TTL;
 * при попадании отдаём из кэша без обращения к источнику.
 * Инвалидация: при изменении категории — удалить ключ из Redis.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    /** Время жизни категории в кэше. */
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final CategoryRepository repository;

    /**
     * Категория с кэшированием cache-aside: сначала Redis, при промахе — источник.
     *
     * @return категория, или пусто, если её нет ни в кэше, ни в источнике
     */
    public Optional<Category> getCategory(Long id) {
        throw new UnsupportedOperationException("TODO: кэш → промах → loadFromSource(id) → сохранить в Redis с TTL");
    }

    /** Источник справочника (настоящая БД/коллекция). */
    private Optional<Category> loadFromSource(Long id) {
        throw new UnsupportedOperationException("TODO: чтение категории из источника (БД)");
    }
}