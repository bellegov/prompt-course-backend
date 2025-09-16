package com.promptcourse.progress_service.service;
import com.promptcourse.progress_service.client.CourseServiceClient;
import com.promptcourse.progress_service.dto.AttemptStatusResponse;
import com.promptcourse.progress_service.model.UserLifeStatus;
import com.promptcourse.progress_service.repository.UserLifeStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class AttemptService {

    private static final int MAX_LIVES = 3;
    private static final int COOLDOWN_HOURS = 3;

    private final UserLifeStatusRepository lifeStatusRepository;
    private final CourseServiceClient courseServiceClient;

    // Метод, который проверяет, можно ли пользователю пройти ЛЮБОЙ тест
    public AttemptStatusResponse checkCanAttempt(Long userId, boolean isSubscribed) {
        if (isSubscribed) {
            return AttemptStatusResponse.builder()
                    .canAttempt(true)
                    .message("Подписка активна.")
                    .livesLeft(-1) // Условное обозначение бесконечных жизней
                    .build();
        }

        UserLifeStatus lifeStatus = getOrCreateLifeStatus(userId);

        // Проверяем, не идет ли уже восстановление
        if (lifeStatus.getRecoveryStartedAt() != null &&
                lifeStatus.getRecoveryStartedAt().plusHours(COOLDOWN_HOURS).isBefore(LocalDateTime.now())) {
            // 3 часа прошло, восстанавливаем жизни
            lifeStatus.setLives(MAX_LIVES);
            lifeStatus.setRecoveryStartedAt(null);
            lifeStatusRepository.save(lifeStatus);
        }

        if (lifeStatus.getLives() > 0) {
            return AttemptStatusResponse.builder()
                    .canAttempt(true)
                    .message("Попытка разрешена.")
                    .livesLeft(lifeStatus.getLives())
                    .build();
        } else {
            // Жизней 0, и кулдаун еще не прошел
            return AttemptStatusResponse.builder()
                    .canAttempt(false)
                    .message("Жизни закончились. Полное восстановление через 3 часа после первой потраченной жизни.")
                    .livesLeft(0)
                    .build();
        }
    }

    // Метод, который вызывается при неудачной попытке
    public void recordFailedAttempt(Long userId, boolean isSubscribed) {
        if (isSubscribed) {
            return; // У подписчиков жизни не тратятся
        }

        UserLifeStatus lifeStatus = getOrCreateLifeStatus(userId);

        if (lifeStatus.getLives() > 0) {
            // Если это первая потраченная жизнь, запускаем таймер
            if (lifeStatus.getLives() == MAX_LIVES) {
                lifeStatus.setRecoveryStartedAt(LocalDateTime.now());
            }
            lifeStatus.setLives(lifeStatus.getLives() - 1);
            lifeStatusRepository.save(lifeStatus);
        }
        // После списания жизни, немедленно сбрасываем кэш в course-service
        try {
            courseServiceClient.clearOutlineCache(userId);
            log.info("Cache invalidation requested for userId {} after failed attempt.", userId);
        } catch (Exception e) {
            log.error("Failed to request cache invalidation for userId {} after failed attempt.", userId, e);
        }
        // -------------------------

    }

    // Вспомогательный метод для получения или создания записи о жизнях
    private UserLifeStatus getOrCreateLifeStatus(Long userId) {
        return lifeStatusRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserLifeStatus newStatus = UserLifeStatus.builder()
                            .userId(userId)
                            .lives(MAX_LIVES)
                            .build();
                    return lifeStatusRepository.save(newStatus);
                });
    }
}
