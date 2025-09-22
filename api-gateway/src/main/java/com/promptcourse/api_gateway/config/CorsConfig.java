package com.promptcourse.api_gateway.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // --- РАЗРЕШЕННЫЕ ИСТОЧНИКИ (ВАЖНО!) ---
        // Добавляем URL фронтенда для локальной разработки и для продакшена
        corsConfig.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://promptly.by"
        ));

        // --- РАЗРЕШЕННЫЕ МЕТОДЫ ---
        corsConfig.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name() // OPTIONS критически важен для preflight-запросов
        ));

        // --- РАЗРЕШЕННЫЕ ЗАГОЛОВКИ ---
        corsConfig.addAllowedHeader("*");

        // --- РАЗРЕШЕНИЕ ПЕРЕДАЧИ "ЧУВСТВИТЕЛЬНЫХ" ДАННЫХ ---
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Применяем эту конфигурацию ко всем путям
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
