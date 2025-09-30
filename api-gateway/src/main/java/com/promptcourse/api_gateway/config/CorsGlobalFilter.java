package com.promptcourse.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//@Component
@Slf4j
public class CorsGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        HttpHeaders headers = response.getHeaders();

        // Добавляем CORS заголовки ко ВСЕМ ответам
        String origin = request.getHeaders().getOrigin();
        if (origin != null && (origin.equals("https://promtly.by") || origin.startsWith("http://localhost:5173"))) {
            headers.add("Access-Control-Allow-Origin", origin);
        } else {
            headers.add("Access-Control-Allow-Origin", "https://promtly.by");
        }

        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        headers.add("Access-Control-Allow-Headers", "*");
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Expose-Headers", "Authorization, X-User-ID, X-User-Roles, X-User-Subscribed");
        headers.add("Access-Control-Max-Age", "3600");

        log.info("CORS headers added for request: {} {} from origin: {}",
                request.getMethod(), request.getURI().getPath(), origin);

        // Если это preflight OPTIONS запрос - отвечаем сразу
        if (request.getMethod() == HttpMethod.OPTIONS) {
            log.info("Handling OPTIONS preflight request for: {}", request.getURI().getPath());
            response.setStatusCode(HttpStatus.OK);
            return response.setComplete();
        }

        // Продолжаем цепочку для других запросов
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Самый высокий приоритет - выполняется первым
    }
}