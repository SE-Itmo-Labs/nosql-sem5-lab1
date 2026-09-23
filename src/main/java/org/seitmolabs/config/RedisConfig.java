package org.seitmolabs.config;

import org.springframework.context.annotation.Configuration;

/**
 * RedisConfig — конфигурация работы с Redis.
 * <p>
 * Назначение: настроить ObjectMapper и RedisTemplate под JSON-сериализацию.
 * Вместе с автоконфигурируемым StringRedisTemplate (используется в repository-слое)
 * этот бин позволяет хранить и читать объекты моделей как JSON-строки.
 */
@Configuration
public class RedisConfig {

    
}