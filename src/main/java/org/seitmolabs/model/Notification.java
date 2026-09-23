package org.seitmolabs.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Notification — уведомление клиента кинотеатра (основной объект системы).
 * <p>
 * Назначение: модель обязательного сценария «отправка уведомлений».
 * В Redis будет храниться как JSON по ключу {@code notification:{id}},
 * индекс клиента — в sorted set {@code notification:user:{userId}},
 * id выдаётся счётчиком {@code notification:seq} (INCR).
 * <p>
 * Геттеры/сеттеры/конструкторы генерирует Lombok.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    /** id уведомления (берётся из Redis-счётчика INCR) */
    private Long id;

    /** id клиента-получателя */
    private String userId;

    private String title;

    private String text;

    private String categoryId;

    /** Статус: NEW / SENT / READ */
    private String status;

    private Instant createdAt;
}