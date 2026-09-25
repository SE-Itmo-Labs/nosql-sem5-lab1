package org.seitmolabs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block {

    private String resourceKey;

    private String owner;

    private long ttlSeconds;

    private Instant createdAt;
}