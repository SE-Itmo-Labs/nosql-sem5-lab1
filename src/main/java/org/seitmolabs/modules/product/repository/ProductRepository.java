package org.seitmolabs.modules.product.repository;

import java.util.List;

import org.seitmolabs.modules.product.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, Long> {

    List<Product> findAllByAvailableTrueOrderByNameAsc();

    boolean existsByName(String name);
}
