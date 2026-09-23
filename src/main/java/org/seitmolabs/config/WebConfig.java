package org.seitmolabs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig — настройка HTTP-слоя приложения.
 * <p>
 * Назначение: разрешить CORS-запросы к API.
 * Фронтенд живёт на другом origin (GitHub Pages / localhost при разработке),
 * поэтому без этих заголовков браузер заблокирует обращения к бекенду.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:3000",
                        "https://se-itmo-labs.github.io",
                        "http://se-itmo-labs.ssngn.ru",
                        "https://se-itmo-labs.ssngn.ru",
                        "https://backend.se-itmo-labs.ssngn.ru")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}