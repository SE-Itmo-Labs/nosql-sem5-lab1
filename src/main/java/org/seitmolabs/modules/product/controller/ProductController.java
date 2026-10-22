package org.seitmolabs.modules.product.controller;

import java.util.List;

import org.seitmolabs.modules.product.dto.ProductResponse;
import org.seitmolabs.modules.product.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public List<ProductResponse> getAvailable() {
        return productRepository.findAllByAvailableTrueOrderByNameAsc().stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.isAvailable()
                ))
                .toList();
    }
}
