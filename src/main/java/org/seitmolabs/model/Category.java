package org.seitmolabs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Category — категория/справочник (тип уведомления, жанр и т.п.).
 * <p>
 * Назначение: сценарий кэширования справочника категорий (cache-aside).
 * В Redis хранится по ключу {@code category:{id}}; повторные чтения
 * должны отдаваться из кэша с TTL, а не из источника.
 * <p>
 * Геттеры/сеттеры/конструкторы генерирует Lombok.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private Long id;

    private String name;

    private String description;
}