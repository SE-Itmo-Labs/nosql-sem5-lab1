package org.seitmolabs.controller;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.model.Notification;
import org.seitmolabs.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * NotificationController — REST API обязательного сценария «отправка уведомлений».
 * <p>
 * Назначение: HTTP-обёртка над NotificationService. Эндпоинты:
 * <ul>
 *   <li>POST /api/notifications — создать и «отправить» уведомление;</li>
 *   <li>GET /api/notifications/{id} — уведомление по id;</li>
 *   <li>GET /api/notifications/user/{userId} — все уведомления клиента.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    public ResponseEntity<Notification> create(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String text,
            @RequestParam(required = false) String categoryId) {

        throw new UnsupportedOperationException("TODO: service.create(...) → 201 Created + тело");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getById(@PathVariable Long id) {
        throw new UnsupportedOperationException("TODO: service.findById(id) → 200 / 404");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> listByUser(@PathVariable String userId) {
        throw new UnsupportedOperationException("TODO: service.listByUserId(userId) → 200 + список");
    }
}