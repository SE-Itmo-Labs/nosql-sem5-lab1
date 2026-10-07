package org.seitmolabs.modules.block;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block {

    private String resourceKey;

    private String owner;

    private long ttlSeconds;

    private Instant createdAt;
}