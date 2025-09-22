package com.promptcourse.api_gateway.config;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GatewayFilter, Ordered {

    private final JwtService jwtService;

    private final List<String> adminRoutes = List.of("/api/courses/admin", "/api/users/admin");
    private final List<String> publicRoutes = List.of("/api/users/auth", "/payment/bepaid/webhook");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        log.info("AuthenticationFilter: Processing request: {} {}", request.getMethod(), request.getURI().getPath());

        // Проверяем наличие заголовка Authorization
        if (!request.getHeaders().containsKey("Authorization")) {
            log.warn("Missing Authorization header for path: {}", request.getURI().getPath());
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getOrEmpty("Authorization").get(0);
        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Invalid Authorization header format");
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            if (jwtService.isTokenExpired(token)) {
                log.warn("Token is expired");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            Claims claims = jwtService.extractAllClaims(token);
            String role = claims.get("role").toString();
            String userId = claims.get("userId").toString();
            String isSubscribed = claims.get("isSubscribed").toString();

            log.info("User authenticated: ID={}, Role={}, Subscribed={}", userId, role, isSubscribed);

            // Проверяем права доступа к админским маршрутам
            if (isAdminRoute(request) && !"ADMIN".equals(role)) {
                log.warn("Access denied to admin route for user with role: {}", role);
                return onError(exchange, HttpStatus.FORBIDDEN);
            }

            // Создаем новый запрос с заголовками
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-ID", userId)
                    .header("X-User-Roles", role)
                    .header("X-User-Subscribed", isSubscribed)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            log.info("Headers added successfully, forwarding to service");
            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        log.error("Returning error response: {}", status);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        // CORS заголовки уже добавлены в CorsGlobalFilter
        return response.setComplete();
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return publicRoutes.stream().anyMatch(route -> request.getURI().getPath().startsWith(route));
    }

    private boolean isAdminRoute(ServerHttpRequest request) {
        return adminRoutes.stream().anyMatch(route -> request.getURI().getPath().startsWith(route));
    }

    @Override
    public int getOrder() {
        return 1; // Выполняется после CorsGlobalFilter (порядок = -1)
    }
}