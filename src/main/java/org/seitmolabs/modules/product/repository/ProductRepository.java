package org.seitmolabs.modules.product.repository;

import java.util.List;

import org.seitmolabs.modules.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByAvailableTrueOrderByNameAsc();

    boolean existsByName(String name);
}
