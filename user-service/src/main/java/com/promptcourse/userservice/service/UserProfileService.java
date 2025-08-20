package com.promptcourse.userservice.service;

import com.promptcourse.userservice.client.CourseServiceClient;
import com.promptcourse.userservice.client.ProgressServiceClient;
import com.promptcourse.userservice.dto.SetPasswordRequest;
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
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
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
    }  public void setUserPassword(Long userId, SetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Простая проверка, что пароль не пустой
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        // Хэшируем и сохраняем новый пароль
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // --- НОВЫЙ МЕТОД ДЛЯ УСТАНОВКИ EMAIL ---
    public void setUserEmail(Long userId, String email) {
        // Здесь мы должны использовать нашу проверку на уникальность!
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email " + email + " is already taken.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(email);
        userRepository.save(user);
    }
}