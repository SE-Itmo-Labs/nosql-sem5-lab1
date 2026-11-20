package org.seitmolabs.modules.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestAttribute;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.auth.dto.response.AuthResponse;
import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.user.domain.User;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Пользователи (клиенты)")
public class UserController {

    @Operation(summary = "Получить текущего пользователя")
    @GetMapping("/me")
    public AuthResponse getCurrentUser(
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User user) {
        return AuthResponse.from(user);
    }

    // @Operation(summary = "Получить список пользователей")
    // @GetMapping
    // public ResponseEntity<PageResponse<Object>> getAll(@PageableDefault(page = 0, size = 20) Pageable pageable) {
    //     // TODO: реализовать
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

    // @Operation(summary = "Получить пользователя по ID")
    // @GetMapping("/{id}")
    // public ResponseEntity<Object> getById(@PathVariable String id) {
    //     // TODO: реализовать
    //     return ResponseEntity.ok().build();
    // }

    // @Operation(summary = "Создать пользователя")
    // @PostMapping
    // public ResponseEntity<Object> create(@RequestBody Object request) {
    //     // TODO: реализовать
    //     return ResponseEntity.status(201).build();
    // }

    // @Operation(summary = "Частичное обновление пользователя")
    // @PatchMapping("/{id}")
    // public ResponseEntity<Object> updatePartial(@PathVariable String id, @RequestBody Object request) {
    //     // TODO: реализовать
    //     return ResponseEntity.ok().build();
    // }

    // @Operation(summary = "Полное обновление пользователя")
    // @PutMapping("/{id}")
    // public ResponseEntity<Object> updateFull(@PathVariable String id, @RequestBody Object request) {
    //     // TODO: реализовать
    //     return ResponseEntity.ok().build();
    // }

    // @Operation(summary = "Удалить пользователя")
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> delete(@PathVariable String id) {
    //     // TODO: реализовать
    //     return ResponseEntity.noContent().build();
    // }
}
