package org.seitmolabs.modules.categories.mappers;

import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.domain.CategoryDocument;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryDocument toDocument(Category category) {
        return new CategoryDocument(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSystemCode()
        );
    }

    public Category toCategory(CategoryDocument document) {
        return new Category(
                document.getId(),
                document.getName(),
                document.getDescription(),
                document.getSystemCode()
        );
    }
}
