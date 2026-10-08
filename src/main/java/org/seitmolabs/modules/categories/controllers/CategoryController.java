package org.seitmolabs.modules.categories.controllers;

import org.seitmolabs.modules.categories.domain.Category;
import org.seitmolabs.modules.categories.services.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Справочник категорий (кэшируемый)")
public class CategoryController {

    private final CategoryService service;

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        throw new UnsupportedOperationException("TODO: service.getCategory(id) → 200 / 404");
    }

    // @Operation(summary = "Получить список категорий")
    // @GetMapping
    // public ResponseEntity<PageResponse<Object>> getAll(@PageableDefault(page = 0, size = 20) Pageable pageable) {
    //     // TODO: реализовать (будет кэшироваться Redis'ом по заданию)
    //     return ResponseEntity.ok(PageResponse.<Object>builder()
    //             .content(Collections.emptyList())
    //             .page(pageable.getPageNumber())
    //             .size(pageable.getPageSize())
    //             .totalElements(0L)
    //             .totalPages(0)
    //             .last(true)
    //             .empty(true)
    //             .build());
    // }

    // @Operation(summary = "Получить категорию по ID")
    // @GetMapping("/{id}")
    // public ResponseEntity<Object> getById(@PathVariable String id) {
    //     // TODO: реализовать
    //     return ResponseEntity.ok().build();
    // }

    // @Operation(summary = "Создать категорию")
    // @PostMapping
    // public ResponseEntity<Object> create(@RequestBody Object request) {
    //     // TODO: реализовать
    //     return ResponseEntity.status(201).build();
    // }

    // @Operation(summary = "Частичное обновление категории")
    // @PatchMapping("/{id}")
    // public ResponseEntity<Object> updatePartial(@PathVariable String id, @RequestBody Object request) {
    //     // TODO: реализовать
    //     return ResponseEntity.ok().build();
    // }

    // @Operation(summary = "Полное обновление категории")
    // @PutMapping("/{id}")
    // public ResponseEntity<Object> updateFull(@PathVariable String id, @RequestBody Object request) {
    //     // TODO: реализовать
    //     return ResponseEntity.ok().build();
    // }

    // @Operation(summary = "Удалить категорию")
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> delete(@PathVariable String id) {
    //     // TODO: реализовать
    //     return ResponseEntity.noContent().build();
    // }
}
