package org.seitmolabs.controller;

import lombok.RequiredArgsConstructor;
import org.seitmolabs.dto.CreateNotificationRequest;
import org.seitmolabs.model.Notification;
import org.seitmolabs.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @PostMapping
    public ResponseEntity<Notification> create(
            @RequestBody CreateNotificationRequest request
    ) {
        Notification notification = service.create(
                request.userId(),
                request.title(),
                request.text(),
                request.categoryId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notification);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // TODO: После подключения БД пользователей различать отсутствие пользователя и пустой список уведомлений.
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotifListByUserId(
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(
                service.getNotifListByUserId(userId)
        );
    }
}