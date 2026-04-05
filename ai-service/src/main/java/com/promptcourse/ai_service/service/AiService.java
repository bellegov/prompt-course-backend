package com.promptcourse.ai_service.service;

import com.promptcourse.ai_service.dto.ChatSummaryDto;
import com.promptcourse.ai_service.dto.GroqMessage;
import com.promptcourse.ai_service.entity.Chat;
import com.promptcourse.ai_service.entity.Message;
import com.promptcourse.ai_service.repository.ChatRepository;
import com.promptcourse.ai_service.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatRepository chatRepo;
    private final MessageRepository messageRepo;
    private final GroqService groqService;

    // Создать новый чат
    public Chat createChat(Long userId, String title, String model) {
        Chat chat = new Chat();
        chat.setUserId(userId);
        chat.setTitle(title != null ? title : "Новый чат");
        chat.setModel(model != null ? model : "llama3-8b-8192");
        return chatRepo.save(chat);
    }

    // Список чатов пользователя (без сообщений)
    public List<ChatSummaryDto> getUserChats(Long userId) {
        return chatRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(c -> new ChatSummaryDto(c.getId(), c.getTitle(), c.getModel(), c.getCreatedAt()))
                .toList();
    }

    // Получить один чат со всей историей
    public Chat getChat(Long chatId, Long userId) {
        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Чат не найден"));

        if (!chat.getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Нет доступа к этому чату");
        }

        return chat;
    }

    // Отправить сообщение и получить ответ от AI
    public Mono<Message> sendMessage(
            Long chatId,
            Long userId,
            String content,
            boolean isSubscribed) {

        // 1. Проверяем что чат существует и принадлежит этому юзеру
        Chat chat = getChat(chatId, userId);

        // 2. Сохраняем сообщение пользователя в БД
        Message userMessage = new Message();
        userMessage.setChat(chat);
        userMessage.setRole("user");
        userMessage.setContent(content);
        messageRepo.save(userMessage);

        // 3. Берём ВСЮ историю чата из БД и готовим для Groq
        List<GroqMessage> history = messageRepo
                .findByChatIdOrderByCreatedAtAsc(chatId)
                .stream()
                .map(m -> new GroqMessage(m.getRole(), m.getContent()))
                .toList();

        // 4. Отправляем в Groq и сохраняем ответ
        return groqService.sendMessage(history, chat.getModel(), isSubscribed)
                .map(responseContent -> {
                    Message assistantMessage = new Message();
                    assistantMessage.setChat(chat);
                    assistantMessage.setRole("assistant");
                    assistantMessage.setContent(responseContent);
                    return messageRepo.save(assistantMessage);
                });
    }

    // Удалить чат со всей историей
    public void deleteChat(Long chatId, Long userId) {
        Chat chat = getChat(chatId, userId);
        chatRepo.delete(chat);
    }
}