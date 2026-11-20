package org.seitmolabs.modules.categories.repositories;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.domain.CategoryDocument;
import org.seitmolabs.modules.categories.mappers.CategoryMapper;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CategoryDbRepository {

    private final CategoryMongoRepository mongoRepository;
    private final CategoryMapper mapper;
    private final CategoryIdGenerator idGenerator;

    public Category save(Category category) {
        if (category.getId() == null) {
            category.setId(idGenerator.nextId());

            // Для новой категории insert не перезапишет существующий документ.
            CategoryDocument inserted = mongoRepository.insert(
                    mapper.toDocument(category)
            );

            return mapper.toCategory(inserted);
        }

        CategoryDocument saved = mongoRepository.save(
                mapper.toDocument(category)
        );

        return mapper.toCategory(saved);
    }

    public Optional<Category> findById(Long id) {
        return mongoRepository.findById(id)
                .map(mapper::toCategory);
    }

    public List<Category> findAll() {
        return mongoRepository.findAll().stream()
                .map(mapper::toCategory)
                .toList();
    }

    public boolean existsByName(String name) {
        return mongoRepository.existsByName(name);
    }

    public Optional<Category> findByName(String name) {
        return mongoRepository.findByName(name)
                .map(mapper::toCategory);
    }

    public Optional<Category> findBySystemCode(String systemCode) {
        return mongoRepository.findBySystemCode(systemCode)
                .map(mapper::toCategory);
    }

    public void deleteById(Long id) {
        mongoRepository.deleteById(id);
    }
}
