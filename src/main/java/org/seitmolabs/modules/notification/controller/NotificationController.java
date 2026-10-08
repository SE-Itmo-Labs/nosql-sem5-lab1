package org.seitmolabs.modules.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.service.NotificationService;
import org.seitmolabs.modules.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Получить уведомление по ID")
    @GetMapping("/{id}")
    public Notification getById(@PathVariable Long id) {
        return notificationService.getById(id);
    }

    @Operation(summary = "Создать уведомление")
    @PostMapping
    public ResponseEntity<Notification> create(@Valid @RequestBody CreateNotificationRequest request) {
        Notification notification = notificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }
}
