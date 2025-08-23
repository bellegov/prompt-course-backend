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

                // Публичные маршруты (без фильтра)
                .route("auth-service-public", r -> {
                    log.info("Setting up auth-service-public route");
                    return r.path("/api/users/auth/**")
                            .filters(f -> f.stripPrefix(2))
                            .uri("lb://USER-SERVICE");
                })

                .route("webhook-service-public", r -> {
                    log.info("Setting up webhook-service-public route");
                    return r.path("/payment/yoomoney/webhook")
                            .uri("lb://USER-SERVICE");
                })

                // Защищенные маршруты (с фильтром)
                .route("user-service-secured", r -> {
                    log.info("Setting up user-service-secured route");
                    return r.path("/api/users/**")
                            .and()
                            .not(nr -> nr.path("/api/users/auth/**"))
                            .filters(f -> f.filter(filter).stripPrefix(2))
                            .uri("lb://USER-SERVICE");
                })

                .route("course-service-secured", r -> {
                    log.info("Setting up course-service-secured route");
                    return r.path("/api/courses/**")
                            .filters(f -> f.filter(filter).stripPrefix(2))
                            .uri("lb://COURSE-SERVICE");
                })

                .route("progress-service-secured", r -> {
                    log.info("Setting up progress-service-secured route");
                    return r.path("/api/progress/**")
                            .filters(f -> f.filter(filter).stripPrefix(2))
                            .uri("lb://PROGRESS-SERVICE");
                })

                .build();

        log.info("=== Routes created successfully ===");
        return routeLocator;
    }
}