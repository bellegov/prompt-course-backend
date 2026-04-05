package com.promptcourse.ai_service.service;

import com.promptcourse.ai_service.dto.GroqMessage;
import com.promptcourse.ai_service.dto.GroqRequest;
import com.promptcourse.ai_service.dto.GroqResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class GroqService {

    // Бесплатные модели — доступны всем
    private static final List<String> FREE_MODELS = List.of(
            "llama-3.3-70b-versatile",  // новая бесплатная основная
            "llama-3.1-8b-instant"      // быстрая бесплатная
    );

    private static final List<String> PREMIUM_MODELS = List.of(
            "meta-llama/llama-4-scout-17b-16e-instruct",
            "meta-llama/llama-4-maverick-17b-128e-instruct"
    );

    private final WebClient webClient;

    public GroqService(@Value("${groq.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<String> sendMessage(
            List<GroqMessage> history,
            String model,
            boolean isSubscribed) {

        // Проверяем подписку для платных моделей
        if (PREMIUM_MODELS.contains(model) && !isSubscribed) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.PAYMENT_REQUIRED,
                    "Эта модель доступна только по подписке"
            ));
        }

        // Если модель не из известных — используем бесплатную
        String finalModel = FREE_MODELS.contains(model) || PREMIUM_MODELS.contains(model)
                ? model
                : "llama-3.3-70b-versatile";

        GroqRequest request = new GroqRequest(finalModel, history);

        return webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Groq ошибка: " + body))
                )
                .bodyToMono(GroqResponse.class)
                .map(r -> r.getChoices().get(0).getMessage().getContent());
    }
}