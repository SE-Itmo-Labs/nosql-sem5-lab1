package org.seitmolabs.modules.notification.controller;

import org.seitmolabs.common.dto.CreateNotificationRequest;
import org.seitmolabs.modules.auth.authorization.RequireRole;
import org.seitmolabs.modules.auth.filters.SimpleAuthFilter;
import org.seitmolabs.modules.notification.domain.Notification;
import org.seitmolabs.modules.notification.service.NotificationService;
import org.seitmolabs.modules.user.domain.User;
import org.seitmolabs.modules.user.enums.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@RequireRole(Role.ROLE_ADMIN)
@Tag(name = "Admin notifications", description = "Ручная отправка и обработка уведомлений")
public class AdminNotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Отправить уведомление выбранному клиенту")
    @PostMapping
    public ResponseEntity<Notification> create(
            @Valid @RequestBody CreateNotificationRequest request,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User currentUser
    ) {
        Notification notification = notificationService.create(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @Operation(summary = "Применить правило обработки к уведомлению")
    @PostMapping("/{id}/apply-rule")
    public Notification applyRule(
            @PathVariable Long id,
            @Valid @RequestBody ApplyRuleRequest request,
            @RequestAttribute(SimpleAuthFilter.CURRENT_USER_ATTRIBUTE) User currentUser
    ) {
        return notificationService.applyRule(id, request.ruleId(), currentUser.getUsername());
    }

    public record ApplyRuleRequest(@NotNull @Positive Long ruleId) {
    }
}
