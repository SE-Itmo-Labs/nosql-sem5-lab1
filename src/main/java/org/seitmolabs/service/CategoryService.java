package org.seitmolabs.service;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Category;
import org.seitmolabs.repository.CategoryCacheRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final CategoryCacheRepository cacheRepository;

    public Optional<Category> getCategory(Long id) {
        Optional<Category> cachedCategory = cacheRepository.findById(id);

        if (cachedCategory.isPresent()) {
            return cachedCategory;
        }

        Optional<Category> category = loadFromSource(id);

        category.ifPresent(categoryFromDb ->
                cacheRepository.put(categoryFromDb, CACHE_TTL)
        );

        return category;
    }

    // TODO: После подключения реляционной БД загружать категории через CategoryDbRepository.
    private Optional<Category> loadFromSource(Long id) {
        throw new UnsupportedOperationException(
                "Источник категорий в реляционной БД ещё не подключён"
        );
    }
}