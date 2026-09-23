package org.seitmolabs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Block — временная блокировка ресурса (например, бронь места в зале).
 * <p>
 * Назначение: сценарий TTL — временное хранение.
 * Ключ {@code block:{resourceId}} живёт ровно {@code ttlSeconds} и исчезает сам
 * (SET ... EX n), после чего ресурс снова свободен — отдельная очистка не нужна.
 * <p>
 * Геттеры/сеттеры/конструкторы генерирует Lombok.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block {

    /** Какой ресурс заблокирован (место, id ресурса). */
    private String resourceId;

    /** Кто держит блокировку (клиент/заявка). */
    private String owner;

    /** Время жизни блокировки в секундах (TTL ключа). */
    private long ttlSeconds;

    private Instant createdAt;
}