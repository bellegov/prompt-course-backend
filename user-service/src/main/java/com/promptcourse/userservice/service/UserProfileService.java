package com.promptcourse.userservice.service;

import com.promptcourse.userservice.client.CourseServiceClient;
import com.promptcourse.userservice.client.ProgressServiceClient;
import com.promptcourse.userservice.dto.LinkAccountRequest;
import com.promptcourse.userservice.dto.UserProfileDto;
import com.promptcourse.userservice.dto.course.UserPromptsDto;
import com.promptcourse.userservice.dto.progress.UserStats;
import com.promptcourse.userservice.model.SubscriptionStatus;
import com.promptcourse.userservice.model.User;
import com.promptcourse.userservice.repository.SubscriptionRepository;
import com.promptcourse.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final ProgressServiceClient progressServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final CourseServiceClient courseServiceClient;
    private final SubscriptionRepository subscriptionRepository;

    public UserProfileDto getUserProfile(Long userId) {
        // 1. Получаем основные данные о пользователе из нашей БД
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Делаем межсервисный запрос за статистикой
        UserStats stats = progressServiceClient.getStatsForUser(userId);

        boolean isSubscribed = subscriptionRepository.findByUserId(userId)
                .map(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE && sub.getEndDate().isAfter(LocalDateTime.now()))
                .orElse(false);

        // 3. Собираем все в один красивый DTO
        return UserProfileDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatarId(user.getAvatarId())
                .isSubscribed(isSubscribed) // <-- Используем реальное значение
                .telegramId(user.getTelegramId())
                .totalLecturesCompleted(stats.getTotalLecturesCompleted())
                .consecutiveDays(stats.getConsecutiveDays())
                .totalActiveDays(stats.getTotalActiveDays())
                .build();
    }
    public List<UserPromptsDto> getUserPrompts(Long userId) {
        // 1. Узнаем, какие лекции прошел пользователь
        Set<Long> completedLectureIds = progressServiceClient.getCompletedLectureIds(userId);

        if (completedLectureIds.isEmpty()) {
            return List.of(); // Возвращаем пустой список, если ничего не пройдено
        }
        // 2. Запрашиваем промпты для этих лекций
        return courseServiceClient.getPromptsByLectures(new ArrayList<>(completedLectureIds));
    }

    public void updateUserAvatar(Long userId, Integer newAvatarId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAvatarId(newAvatarId);
        userRepository.save(user);
    }

    @Transactional
    public void linkAccount(Long telegramUserId, LinkAccountRequest request) {
        // 1. Ищем текущий Telegram-аккаунт (от которого пришел запрос)
        User telegramUser = userRepository.findById(telegramUserId)
                .orElseThrow(() -> new RuntimeException("Current Telegram user not found"));

        // 2. Ищем, существует ли уже аккаунт с таким email
        Optional<User> existingSiteUserOpt = userRepository.findByEmail(request.getEmail());

        if (existingSiteUserOpt.isPresent()) {
            // --- СЦЕНАРИЙ СЛИЯНИЯ ---
            User siteUser = existingSiteUserOpt.get();

            // Проверяем, что мы не пытаемся слить аккаунт сам с собой
            if (telegramUser.getId().equals(siteUser.getId())) {
                throw new IllegalStateException("This email is already linked to your account.");
            }

            // Проверяем пароль от "старого" аккаунта
            if (siteUser.getPassword() == null || !passwordEncoder.matches(request.getPassword(), siteUser.getPassword())) {
                throw new IllegalArgumentException("Invalid password for the existing account.");
            }

            // Проверяем, что у "старого" аккаунта еще не привязан другой Telegram
            if (siteUser.getTelegramId() != null) {
                throw new IllegalStateException("The site account is already linked to another Telegram account.");
            }

            // Запоминаем telegram_id для переноса
            Long telegramIdToMerge = telegramUser.getTelegramId();

            // TODO: Перенести прогресс
            // progressService.mergeProgress(telegramUser.getId(), siteUser.getId());

            // Сначала удаляем временный telegram-аккаунт
            userRepository.delete(telegramUser);
            userRepository.flush(); // Принудительно применяем удаление

            // Затем обновляем основной аккаунт
            siteUser.setTelegramId(telegramIdToMerge);
            userRepository.save(siteUser);

        } else {
            // --- СЦЕНАРИЙ "ДООПРЕДЕЛЕНИЯ" ---
            // Email свободен. Просто дополняем текущий Telegram-аккаунт.
            telegramUser.setEmail(request.getEmail());
            telegramUser.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(telegramUser);
        }
    }

}