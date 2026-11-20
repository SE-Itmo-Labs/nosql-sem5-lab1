package org.seitmolabs.demo;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.repositories.CategoryDbRepository;
import org.springframework.core.annotation.Order;

@Order(20)
@RequiredArgsConstructor
class CategoriesDemo implements DemoScenario {
    private final CategoryDbRepository categoryRepository;

    @Override
    public void run() {
        createCategoryIfMissing("Сеансы", "Изменения времени и зала");
        createCategoryIfMissing("Акции", "Скидки и специальные предложения");
        createCategoryIfMissing("Премьеры", "Новые фильмы и специальные показы");
        createCategoryIfMissing("Возвраты", "Возврат билетов и отмена заказов");
    }

    private void createCategoryIfMissing(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            return;
        }

        categoryRepository.save(new Category(null, name, description));
    }
}
