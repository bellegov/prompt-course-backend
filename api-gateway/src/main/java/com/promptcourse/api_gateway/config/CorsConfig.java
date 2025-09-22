package com.promptcourse.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // --- РАЗРЕШЕННЫЕ ИСТОЧНИКИ ---
        // Добавляем URL фронтенда для локальной разработки и для продакшена
        corsConfig.setAllowedOrigins(List.of("http://localhost:5173", "https://promtly.by"));

        corsConfig.setMaxAge(3600L); // Время, на которое браузер кэширует результат OPTIONS-запроса (1 час)
        corsConfig.addAllowedMethod("*"); // Разрешаем все HTTP-методы (GET, POST, PUT, DELETE)
        corsConfig.addAllowedHeader("*"); // Разрешаем все заголовки
        corsConfig.setAllowCredentials(true); // Разрешаем передачу cookies и заголовка Authorization

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Применяем эту конфигурацию ко всем путям ("/**")
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
