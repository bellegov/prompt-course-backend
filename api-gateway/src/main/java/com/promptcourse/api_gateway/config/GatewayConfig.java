package com.promptcourse.api_gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GatewayConfig {

    private final AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        log.info("=== Creating routes ===");

        RouteLocator routeLocator = builder.routes()

                // ---------- USER-SERVICE ----------
                // Публичные ручки авторизации (/api/users/auth/**)
                .route("auth-service-public", r -> {
                    log.info("Setting up auth-service-public route");
                    return r.path("/api/users/auth/**")
                            .filters(f -> f.stripPrefix(2))
                            .uri("lb://USER-SERVICE");
                })
                // Альтернативный вариант без /api (/users/auth/**)
                .route("auth-service-public-alt", r -> {
                    log.info("Setting up auth-service-public-alt route");
                    return r.path("/users/auth/**")
                            .filters(f -> f.stripPrefix(1))
                            .uri("lb://USER-SERVICE");
                })

                // Защищённые ручки пользователей (/api/users/**)
                .route("user-service-secured", r -> {
                    log.info("Setting up user-service-secured route");
                    return r.path("/api/users/**")
                            .and().not(nr -> nr.path("/api/users/auth/**"))
                            .filters(f -> f.filter(filter).stripPrefix(2))
                            .uri("lb://USER-SERVICE");
                })
                // Альтернативный вариант без /api (/users/**)
                .route("user-service-secured-alt", r -> {
                    log.info("Setting up user-service-secured-alt route");
                    return r.path("/users/**")
                            .and().not(nr -> nr.path("/users/auth/**"))
                            .filters(f -> f.filter(filter).stripPrefix(1))
                            .uri("lb://USER-SERVICE");
                })

                // ---------- COURSE-SERVICE ----------
                .route("course-service-secured", r -> {
                    log.info("Setting up course-service-secured route");
                    return r.path("/api/courses/**")
                            .filters(f -> f.filter(filter).stripPrefix(2))
                            .uri("lb://COURSE-SERVICE");
                })
                .route("course-service-secured-alt", r -> {
                    log.info("Setting up course-service-secured-alt route");
                    return r.path("/courses/**")
                            .filters(f -> f.filter(filter).stripPrefix(1))
                            .uri("lb://COURSE-SERVICE");
                })

                // ---------- PROGRESS-SERVICE ----------
                .route("progress-service-secured", r -> {
                    log.info("Setting up progress-service-secured route");
                    return r.path("/api/progress/**")
                            .filters(f -> f.filter(filter).stripPrefix(2))
                            .uri("lb://PROGRESS-SERVICE");
                })
                .route("progress-service-secured-alt", r -> {
                    log.info("Setting up progress-service-secured-alt route");
                    return r.path("/progress/**")
                            .filters(f -> f.filter(filter).stripPrefix(1))
                            .uri("lb://PROGRESS-SERVICE");
                })

                // ---------- WEBHOOK (публичный) ----------
                .route("webhook-service-public", r -> {
                    log.info("Setting up webhook-service-public route");
                    return r.path("/payment/yoomoney/webhook")
                            .uri("lb://USER-SERVICE");
                })

                .build();

        log.info("=== Routes created successfully ===");
        return routeLocator;
    }
}