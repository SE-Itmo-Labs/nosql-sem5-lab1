package org.seitmolabs.modules.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.dto.NotificationSearchPage;
import org.seitmolabs.modules.notification.dto.NotificationSearchRequest;
import org.seitmolabs.modules.notification.service.NotificationService;
import org.seitmolabs.modules.user.domain.User;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor 
@Tag(name = "Notifications", description = "Управление уведомлениями")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Получить свои уведомления")
    @GetMapping
    public List<Notification> getMyNotifications(
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User currentUser
    ) {
        return notificationService.getByUserId(currentUser.getUsername());
    }

    @Operation(summary = "Найти свои уведомления по фильтрам и журналу действий")
    @GetMapping("/search")
    public ResponseEntity<NotificationSearchPage> search(
            @Valid @ModelAttribute @ParameterObject NotificationSearchRequest request,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User currentUser
    ) {
        var result = notificationService.search(currentUser.getUsername(), request);
        return ResponseEntity.ok().header("X-Cache", result.cacheStatus().name()).body(result.page());
    }

    @Operation(summary = "Получить уведомление по ID")
    @GetMapping("/{id}")
    public Notification getById(
            @PathVariable Long id,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User currentUser
    ) {
        return notificationService.getByIdForUser(id, currentUser.getUsername());
    }

    @Operation(summary = "Отметить своё уведомление прочитанным")
    @PostMapping("/{id}/read")
    public Notification markRead(
            @PathVariable Long id,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User currentUser
    ) {
        return notificationService.markRead(id, currentUser.getUsername());
    }

    @Operation(summary = "Удалить своё уведомление")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User currentUser
    ) {
        notificationService.delete(id, currentUser.getUsername());
        return ResponseEntity.noContent().build();
    }

}
