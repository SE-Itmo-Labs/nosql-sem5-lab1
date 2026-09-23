package org.seitmolabs.controller;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Category;
import org.seitmolabs.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CategoryController — REST API сценария кэширования справочника категорий.
 * <p>
 * Назначение: HTTP-обёртка над CategoryService (cache-aside).
 * Эндпоинт: GET /api/categories/{id} — категория; повторные запросы
 * должны отдаваться из кэша Redis, без обращения к источнику.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService service;

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        throw new UnsupportedOperationException("TODO: service.getCategory(id) → 200 / 404");
    }
}