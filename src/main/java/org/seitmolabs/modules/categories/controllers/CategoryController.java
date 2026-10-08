package org.seitmolabs.modules.categories.controllers;

import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.dto.CategoryRequest;
import org.seitmolabs.modules.categories.dto.CategoryResponse;
import org.seitmolabs.modules.categories.services.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(summary = "Получить категорию и состояние кэша")
    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable Long id) {
        return service.getCategory(id);
    }

    @Operation(summary = "Создать категорию")
    @PostMapping
    public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Обновить категорию")
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Удалить категорию")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
