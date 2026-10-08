package org.seitmolabs.modules.categories.repositories;

import org.seitmolabs.modules.categories.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryDbRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
}
