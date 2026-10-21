package org.seitmolabs.modules.notification.controller;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.auth.authorization.RequireRole;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.service.NotificationService;
import org.seitmolabs.modules.user.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@RequireRole(Role.ROLE_ADMIN)
@Tag(name = "Admin notifications", description = "Ручная отправка уведомлений")
public class AdminNotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Отправить уведомление выбранному клиенту")
    @PostMapping
    public ResponseEntity<Notification> create(@Valid @RequestBody CreateNotificationRequest request) {
        Notification notification = notificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }
}
