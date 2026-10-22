package org.seitmolabs.modules.categories.controllers;

import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.services.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Справочник категорий (кэшируемый)")
public class CategoryController {

    private final CategoryService service;

    @Operation(summary = "Получить список категорий")
    @GetMapping
    public List<Category> getAll() {
        return service.getAll();
    }

}
