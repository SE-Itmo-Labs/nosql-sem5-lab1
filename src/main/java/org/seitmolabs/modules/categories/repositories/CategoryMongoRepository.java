package org.seitmolabs.modules.categories.repositories;

import java.util.Optional;

import org.seitmolabs.modules.categories.domain.CategoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryMongoRepository extends MongoRepository<CategoryDocument, String> {

    boolean existsByName(String name);

    Optional<CategoryDocument> findByName(String name);

    Optional<CategoryDocument> findBySystemCode(String systemCode);
}
