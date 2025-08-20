package com.promptcourse.progressservice.service;

import com.promptcourse.progressservice.model.UserStats;
import com.promptcourse.progressservice.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UserStatsService {

    private final UserStatsRepository statsRepository;

    // Метод, который вызывается при входе пользователя
    @Transactional
    public void processDailyLogin(Long userId) {
        UserStats stats = getOrCreateStats(userId);
        LocalDate today = LocalDate.now();

        // Если пользователь уже заходил сегодня, ничего не делаем
        if (stats.getLastLoginDate() != null && stats.getLastLoginDate().isEqual(today)) {
            return;
        }

        // --- Логика "Дней подряд" ---
        // Если последний заход был вчера, увеличиваем страйк
        if (stats.getLastLoginDate() != null && ChronoUnit.DAYS.between(stats.getLastLoginDate(), today) == 1) {
            stats.setConsecutiveDays(stats.getConsecutiveDays() + 1);
        } else {
            // Если был пропуск (или это первый заход), сбрасываем страйк до 1
            stats.setConsecutiveDays(1);
        }

        // --- Логика "Дней в приложении" ---
        stats.setTotalActiveDays(stats.getTotalActiveDays() + 1);

        // Обновляем дату последнего захода
        stats.setLastLoginDate(today);

        statsRepository.save(stats);
    }

    // Метод, который вызывается при прохождении лекции
    @Transactional
    public void incrementLecturesCompleted(Long userId) {
        UserStats stats = getOrCreateStats(userId);
        stats.setTotalLecturesCompleted(stats.getTotalLecturesCompleted() + 1);
        statsRepository.save(stats);
    }

    // Метод для получения статистики
    public UserStats getStatsForUser(Long userId) {
        return getOrCreateStats(userId);
    }

    // Вспомогательный метод
    private UserStats getOrCreateStats(Long userId) {
        return statsRepository.findByUserId(userId).orElseGet(() ->
                statsRepository.save(UserStats.builder().userId(userId).build())
        );
    }
}
