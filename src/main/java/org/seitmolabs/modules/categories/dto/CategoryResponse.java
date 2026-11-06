package org.seitmolabs.modules.categories.dto;

import org.seitmolabs.modules.categories.domain.Category;

public record CategoryResponse(
        Category category,
        CategoryCacheInfo cache
) {
}
