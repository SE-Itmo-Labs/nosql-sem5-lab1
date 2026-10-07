package org.seitmolabs.modules.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.seitmolabs.modules.notification.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor 
@Tag(name = "Notifications", description = "Управление уведомлениями")
public class NotificationController {

    // private final NotificationService notificationService;

    // @Operation(summary = "Получить список уведомлений", description = "Список с фильтрами, пагинацией, сортировкой")
    // @GetMapping
    // public ResponseEntity<PageResponse<NotificationResponse>> getAll(
    //         @PageableDefault(page = 0, size = 20, sort = "sentAt") Pageable pageable,
    //         NotificationFilter filter
    // ) {
    //     // TODO: реализовать в сервисе
    //     return ResponseEntity.ok(notificationService.getAll(pageable, filter));
    // }

    // @Operation(summary = "Поиск уведомлений", description = "Полнотекстовый/расширенный поиск по ?q=")
    // @GetMapping("/search")
    // public ResponseEntity<PageResponse<NotificationResponse>> search(
    //         @PageableDefault(page = 0, size = 20) Pageable pageable,
    //         @RequestParam(name = "q", required = false) String q
    // ) {
    //     // TODO: реализовать в сервисе
    //     return ResponseEntity.ok(notificationService.search(pageable, q));
    // }

    // @Operation(summary = "Количество уведомлений", description = "Количество с учётом фильтров")
    // @GetMapping("/count")
    // public ResponseEntity<Long> count(NotificationFilter filter) {
    //     // TODO: реализовать в сервисе
    //     return ResponseEntity.ok(notificationService.count(filter));
    // }

    // @Operation(summary = "Статистика по уведомлениям", description = "Агрегации: группировки, avg/min/max, топы")
    // @GetMapping("/stats")
    // public ResponseEntity<Object> getStats(NotificationFilter filter) {
    //     // TODO: реализовать в сервисе
    //     return ResponseEntity.ok(notificationService.getStats(filter));
    // }

    // @Operation(summary = "Получить уведомление по ID")
    // @GetMapping("/{id}")
    // public ResponseEntity<NotificationResponse> getById(@PathVariable String id) {
    //     // TODO: реализовать в сервисе
    //     return ResponseEntity.ok(notificationService.getById(id));
    // }

    // @Operation(summary = "Создать уведомление")
    // @PostMapping
    // public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationCreateRequest request) {
    //     // TODO: реализовать в сервисе
    //     NotificationResponse created = notificationService.create(request);
    //     return ResponseEntity.status(HttpStatus.CREATED).body(created);
    // }

    // @Operation(summary = "Частичное обновление уведомления")
    // @PatchMapping("/{id}")
    // public ResponseEntity<NotificationResponse> updatePartial(
    //         @PathVariable String id,
    //         @Valid @RequestBody NotificationUpdateRequest request
    // ) {
    //     // TODO: реализовать в сервисе
    //     return ResponseEntity.ok(notificationService.updatePartial(id, request));
    // }

    // @Operation(summary = "Полное обновление уведомления")
    // @PutMapping("/{id}")
    // public ResponseEntity<NotificationResponse> updateFull(
    //         @PathVariable String id,
    //         @Valid @RequestBody NotificationUpdateRequest request
    // ) {
    //     // TODO: реализовать в сервисе
    //     return ResponseEntity.ok(notificationService.updateFull(id, request));
    // }

    // @Operation(summary = "Удалить уведомление")
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> delete(@PathVariable String id) {
    //     // TODO: реализовать в сервисе
    //     notificationService.delete(id);
    //     return ResponseEntity.noContent().build();
    // }
}