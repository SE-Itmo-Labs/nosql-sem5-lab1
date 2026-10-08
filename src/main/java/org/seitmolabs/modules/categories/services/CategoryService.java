package org.seitmolabs.modules.categories.services;

import lombok.RequiredArgsConstructor;

import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.dto.CategoryCacheInfo;
import org.seitmolabs.modules.categories.dto.CategoryRequest;
import org.seitmolabs.modules.categories.dto.CategoryResponse;
import org.seitmolabs.modules.categories.repositories.CategoryCacheRepository;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final CategoryCacheRepository cacheRepository;
    private final CategoryDbRepository dbRepository;

    public CategoryResponse getCategory(Long id) {
        Optional<Category> cachedCategory = cacheRepository.findById(id);

        if (cachedCategory.isPresent()) {
            return response(cachedCategory.get(), "CACHE", true);
        }

        Category category = dbRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена: " + id));

        cacheRepository.put(category, CACHE_TTL);
        return response(category, "DATABASE", false);
    }

    public List<Category> getAll() {
        Optional<List<Category>> cachedCategories = cacheRepository.findAll();

        if (cachedCategories.isPresent()) {
            return cachedCategories.get();
        }

        List<Category> categories = dbRepository.findAll();
        cacheRepository.putAll(categories, CACHE_TTL);
        return categories;
    }

    public Category create(CategoryRequest request) {
        Category category = new Category(null, request.name(), request.description());
        Category savedCategory = dbRepository.save(category);
        cacheRepository.deleteList();
        return savedCategory;
    }

    public Category update(Long id, CategoryRequest request) {
        Category category = dbRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Категория не найдена: " + id));

        category.setName(request.name());
        category.setDescription(request.description());

        Category savedCategory = dbRepository.save(category);
        cacheRepository.put(savedCategory, CACHE_TTL);
        cacheRepository.deleteList();
        return savedCategory;
    }

    public void delete(Long id) {
        if (!dbRepository.existsById(id)) {
            throw new EntityNotFoundException("Категория не найдена: " + id);
        }

        dbRepository.deleteById(id);
        cacheRepository.delete(id);
        cacheRepository.deleteList();
    }

    private CategoryResponse response(Category category, String source, boolean cached) {
        long remainingTtl = cacheRepository.getRemainingTtlSeconds(category.getId());

        return new CategoryResponse(
                category,
                new CategoryCacheInfo(source, cached, Math.max(remainingTtl, 0))
        );
    }
}
