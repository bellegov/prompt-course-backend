package com.promptcourse.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsGlobalConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Разрешаем только твой фронтенд
        corsConfig.setAllowedOriginPatterns(List.of("https://promtly.by"));

        // Разрешённые методы
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Разрешённые заголовки (включая кастомный x-user-id)
        corsConfig.setAllowedHeaders(List.of("Authorization", "Content-Type", "x-user-id"));

        // Заголовки, которые будут видны на фронте
        corsConfig.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

        // Разрешаем cookie / Authorization header
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
