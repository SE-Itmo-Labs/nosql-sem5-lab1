package org.seitmolabs.modules.notification.domains;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;

    private String title;

    private String message;

    private String type; // INFO, WARNING, PROMOTION, SYSTEM и т.д.

    @Builder.Default
    private Boolean read = false;

    private Instant sentAt;

    private Instant expiresAt; // для TTL (временные данные)

    private String categoryId;
}