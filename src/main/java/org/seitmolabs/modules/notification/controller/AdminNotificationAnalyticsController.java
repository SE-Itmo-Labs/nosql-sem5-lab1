package org.seitmolabs.modules.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.seitmolabs.modules.auth.authorization.RequireRole;
import org.seitmolabs.modules.notification.dto.NotificationReadAnalytics;
import org.seitmolabs.modules.notification.service.NotificationAnalyticsService;
import org.seitmolabs.modules.user.enums.Role;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequireRole(Role.ROLE_ADMIN)
@RequestMapping("/api/v1/admin/notifications/analytics")
public class AdminNotificationAnalyticsController {
    private final NotificationAnalyticsService analyticsService;

    @Operation(summary = "Время до первого прочтения уведомления за период")
    @GetMapping("/read-times")
    public NotificationReadAnalytics readTimes(@Valid @ModelAttribute @ParameterObject Period period) {
        return analyticsService.readTimes(period.days() == null ? 30 : period.days());
    }

    public record Period(@Min(1) @Max(90) Integer days) {
    }
}
