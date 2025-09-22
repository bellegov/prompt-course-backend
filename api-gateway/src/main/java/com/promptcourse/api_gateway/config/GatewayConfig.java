package com.promptcourse.api_gateway.config;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // --- ПУБЛИЧНЫЕ МАРШРУТЫ (БЕЗ ФИЛЬТРА) ---
                // Правило для ВСЕХ публичных путей user-service
                .route("public-user-routes", r -> r.path("/api/users/auth/**", "/payment/**")
                        .filters(f -> f.stripPrefix(2)) // Отрезаем /api/users
                        .uri("lb://USER-SERVICE"))

                // --- ЗАЩИЩЕННЫЕ МАРШРУТЫ (С ФИЛЬТРОМ) ---
                .route("secured-user-routes", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(filter).stripPrefix(2))
                        .uri("lb://USER-SERVICE"))

                .route("secured-course-routes", r -> r.path("/api/courses/**")
                        .filters(f -> f.filter(filter).stripPrefix(2))
                        .uri("lb://COURSE-SERVICE"))

                .route("secured-progress-routes", r -> r.path("/api/progress/**")
                        .filters(f -> f.filter(filter).stripPrefix(2))
                        .uri("lb://PROGRESS-SERVICE"))
                .build();
    }
}