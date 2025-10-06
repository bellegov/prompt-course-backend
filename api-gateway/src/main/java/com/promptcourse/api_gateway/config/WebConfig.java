package com.promptcourse.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Разрешенные origins
        corsConfig.setAllowedOriginPatterns(List.of(
                "https://promtly.by",
                "http://promtly.by",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        // Разрешенные методы
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Разрешенные заголовки
        corsConfig.setAllowedHeaders(List.of("*"));

        // Разрешить credentials
        corsConfig.setAllowCredentials(true);

        // Expose headers
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-User-ID",
                "X-User-Roles",
                "X-User-Subscribed"
        ));

        // Max age
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}