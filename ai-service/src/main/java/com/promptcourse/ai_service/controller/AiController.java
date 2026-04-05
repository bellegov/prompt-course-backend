package com.promptcourse.ai_service.controller;

import com.promptcourse.ai_service.dto.ChatRequest;
import com.promptcourse.ai_service.dto.ChatSummaryDto;
import com.promptcourse.ai_service.dto.MessageRequest;
import com.promptcourse.ai_service.entity.Chat;
import com.promptcourse.ai_service.entity.Message;
import com.promptcourse.ai_service.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    // Создать новый чат
    @PostMapping("/chats")
    public Chat createChat(
            @RequestBody ChatRequest request,
            @RequestHeader("X-User-ID") Long userId) {
        return aiService.createChat(userId, request.getTitle(), request.getModel());
    }

    // Получить список чатов пользователя
    @GetMapping("/chats")
    public List<ChatSummaryDto> getChats(
            @RequestHeader("X-User-ID") Long userId) {
        return aiService.getUserChats(userId);
    }

    // Получить один чат с историей
    @GetMapping("/chats/{id}")
    public Chat getChat(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") Long userId) {
        return aiService.getChat(id, userId);
    }

    // Отправить сообщение
    @PostMapping("/chats/{id}/message")
    public Mono<Message> sendMessage(
            @PathVariable Long id,
            @RequestBody MessageRequest request,
            @RequestHeader("X-User-ID") Long userId,
            @RequestHeader(value = "X-User-Subscribed", defaultValue = "false") boolean isSubscribed) {
        return aiService.sendMessage(id, userId, request.getContent(), isSubscribed);
    }

    // Удалить чат
    @DeleteMapping("/chats/{id}")
    public void deleteChat(
            @PathVariable Long id,
            @RequestHeader("X-User-ID") Long userId) {
        aiService.deleteChat(id, userId);
    }
}