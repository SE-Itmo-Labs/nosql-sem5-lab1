package org.seitmolabs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private Long id;

    private String userId;

    private String title;

    private String text;

    private String categoryId;

    private Instant createdAt;
}