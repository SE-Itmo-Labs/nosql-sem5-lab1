package org.seitmolabs.modules.categories.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.dto.CategoryRequest;
import org.seitmolabs.modules.categories.dto.CategoryResponse;
import org.seitmolabs.modules.categories.repositories.CategoryCacheRepository;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;

class CategoryServiceTest {

    private CategoryCacheRepository cacheRepository;
    private CategoryDbRepository dbRepository;
    private CategoryService service;

    @BeforeEach
    void setUp() {
        cacheRepository = mock(CategoryCacheRepository.class);
        dbRepository = mock(CategoryDbRepository.class);
        service = new CategoryService(cacheRepository, dbRepository);
    }

    @Test
    void returnsCategoryFromCache() {
        Category category = category();
        when(cacheRepository.findById(1L)).thenReturn(Optional.of(category));
        when(cacheRepository.getRemainingTtlSeconds(1L)).thenReturn(245L);

        CategoryResponse result = service.getCategory(1L);

        assertThat(result.category()).isEqualTo(category);
        assertThat(result.cache().source()).isEqualTo("CACHE");
        assertThat(result.cache().cached()).isTrue();
        assertThat(result.cache().remainingTtlSeconds()).isEqualTo(245);
        verify(dbRepository, never()).findById(1L);
    }

    @Test
    void loadsCategoryFromDatabaseAfterCacheMiss() {
        Category category = category();
        when(cacheRepository.findById(1L)).thenReturn(Optional.empty());
        when(dbRepository.findById(1L)).thenReturn(Optional.of(category));
        when(cacheRepository.getRemainingTtlSeconds(1L)).thenReturn(300L);

        CategoryResponse result = service.getCategory(1L);

        assertThat(result.cache().source()).isEqualTo("DATABASE");
        assertThat(result.cache().cached()).isFalse();
        verify(cacheRepository).put(category, Duration.ofMinutes(5));
    }

    @Test
    void returnsCachedCategoryList() {
        List<Category> categories = List.of(category());
        when(cacheRepository.findAll()).thenReturn(Optional.of(categories));

        List<Category> result = service.getAll();

        assertThat(result).isSameAs(categories);
        verify(dbRepository, never()).findAll();
    }

    @Test
    void updateRefreshesItemAndInvalidatesListCache() {
        Category category = category();
        CategoryRequest request = new CategoryRequest("Новое имя", "Новое описание");
        when(dbRepository.findById(1L)).thenReturn(Optional.of(category));
        when(dbRepository.save(category)).thenReturn(category);

        Category result = service.update(1L, request);

        assertThat(result.getName()).isEqualTo("Новое имя");
        assertThat(result.getDescription()).isEqualTo("Новое описание");
        verify(cacheRepository).put(category, Duration.ofMinutes(5));
        verify(cacheRepository).deleteList();
    }

    @Test
    void deleteInvalidatesItemAndListCache() {
        when(dbRepository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(dbRepository).deleteById(1L);
        verify(cacheRepository).delete(1L);
        verify(cacheRepository).deleteList();
    }

    private Category category() {
        return new Category(1L, "Бронирование", "Изменения состояния билета");
    }
}
