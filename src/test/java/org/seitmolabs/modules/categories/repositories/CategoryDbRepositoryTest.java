package org.seitmolabs.modules.categories.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.domain.CategoryDocument;
import org.seitmolabs.modules.categories.mappers.CategoryMapper;

class CategoryDbRepositoryTest {

    @Test
    void createsCategoryWithNumericId() {
        CategoryMongoRepository mongo = mock(CategoryMongoRepository.class);
        CategoryIdGenerator ids = mock(CategoryIdGenerator.class);
        CategoryDbRepository repository = new CategoryDbRepository(mongo, new CategoryMapper(), ids);
        when(ids.nextId()).thenReturn(42L);
        when(mongo.insert(any(CategoryDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = repository.save(new Category(null, "Название", "Описание"));

        assertThat(result.getId()).isEqualTo(42L);
        verify(mongo).insert(new CategoryDocument(42L, "Название", "Описание", null));
    }

    @Test
    void loadsSystemCategoryAndPreservesItsNumericId() {
        CategoryMongoRepository mongo = mock(CategoryMongoRepository.class);
        CategoryDbRepository repository = new CategoryDbRepository(
                mongo, new CategoryMapper(), mock(CategoryIdGenerator.class));
        when(mongo.findBySystemCode(Category.BOOKING_CODE)).thenReturn(Optional.of(
                new CategoryDocument(7L, "Бронирование", "Описание", Category.BOOKING_CODE)));

        Category category = repository.findBySystemCode(Category.BOOKING_CODE).orElseThrow();

        assertThat(category.getId()).isEqualTo(7L);
        assertThat(category.getSystemCode()).isEqualTo(Category.BOOKING_CODE);
    }
}
