package org.seitmolabs.demo;

import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.product.domain.Product;
import org.seitmolabs.modules.product.repository.ProductRepository;
import org.springframework.core.annotation.Order;

@Order(40)
@RequiredArgsConstructor
class ProductsDemo implements DemoScenario {
    private final ProductRepository productRepository;

    @Override
    public void run() {
        createProductIfMissing("Попкорн солёный", new BigDecimal("290.00"));
        createProductIfMissing("Кола 0,5 л", new BigDecimal("220.00"));
        createProductIfMissing("Начос с соусом", new BigDecimal("340.00"));
    }

    private void createProductIfMissing(String name, BigDecimal price) {
        if (productRepository.existsByName(name)) {
            return;
        }
        productRepository.save(Product.builder()
                .name(name)
                .price(price)
                .available(true)
                .build());
    }
}
