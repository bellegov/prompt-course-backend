package com.promptcourse.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@Slf4j
public class CorsGlobalConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        log.info("=== Configuring CORS ===");

        CorsConfiguration corsConfig = new CorsConfiguration();

        // Разрешаем твой фронтенд (добавляем localhost для разработки)
        corsConfig.setAllowedOriginPatterns(List.of(
                "https://promtly.by",
                "http://localhost:*"  // для локальной разработки
        ));

        // Разрешённые методы (добавляем все необходимые)
        corsConfig.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        // ВАЖНО: Разрешаем ВСЕ заголовки, а не только конкретные
        corsConfig.setAllowedHeaders(List.of("*"));

        // Заголовки, которые будут видны на фронте
        corsConfig.setExposedHeaders(List.of(
                "Authorization",
                "Content-Disposition",
                "X-User-ID",
                "X-User-Roles",
                "X-User-Subscribed"
        ));

        // Разрешаем cookie / Authorization header
        corsConfig.setAllowCredentials(true);

        // Время кэширования preflight запросов (30 минут)
        corsConfig.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        log.info("CORS configured successfully for origins: https://promtly.by");
        return new CorsWebFilter(source);
    }
}