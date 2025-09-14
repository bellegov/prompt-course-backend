package com.promptcourse.progressservice.service;
import com.promptcourse.progressservice.client.CourseServiceClient;
import com.promptcourse.progressservice.model.UserGlobalProgress;
import com.promptcourse.progressservice.repository.UserGlobalProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalProgressService {

    private final UserGlobalProgressRepository globalProgressRepository;
    private final CourseServiceClient courseServiceClient;

    public UserGlobalProgress getGlobalProgress(Long userId) {
        // Находим прогресс пользователя или создаем новый, если это его первый визит
        return globalProgressRepository.findByUserId(userId)
                .orElseGet(() -> globalProgressRepository.save(
                        UserGlobalProgress.builder().userId(userId).build()
                ));
    }

    public void completeSection(Long userId, int sectionOrderIndex) {
        UserGlobalProgress progress = getGlobalProgress(userId);
        // Обновляем, только если новый пройденный раздел идет по порядку
        if (sectionOrderIndex > progress.getLastCompletedSectionOrderIndex()) {
            progress.setLastCompletedSectionOrderIndex(sectionOrderIndex);
            globalProgressRepository.save(progress);
        }
        // --- НОВЫЙ БЛОК: СБРОС КЭША ---
        try {
            courseServiceClient.clearOutlineCache(userId);
            log.info("Successfully requested cache invalidation for user {} after completing a section.", userId);
        } catch (Exception e) {
            log.error("Failed to request cache invalidation for user {} after completing a section.", userId, e);
        }
    }
}
