package com.promptcourse.progress_service.service;
import com.promptcourse.progress_service.client.CourseServiceClient;
import com.promptcourse.progress_service.dto.SectionForProgressDto;
import com.promptcourse.progress_service.model.UserGlobalProgress;
import com.promptcourse.progress_service.repository.CompletedTestRepository;
import com.promptcourse.progress_service.repository.UserGlobalProgressRepository;
import com.promptcourse.progress_service.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalProgressService {

    private final UserGlobalProgressRepository globalProgressRepository;
    private final CourseServiceClient courseServiceClient;
    private final UserProgressRepository userProgressRepository;
    private final CompletedTestRepository completedTestRepository;

    public UserGlobalProgress getGlobalProgress(Long userId) {
        // Находим прогресс пользователя или создаем новый, если это его первый визит
        return globalProgressRepository.findByUserId(userId)
                .orElseGet(() -> globalProgressRepository.save(
                        UserGlobalProgress.builder().userId(userId).build()
                ));
    }

    @Transactional
    public void completeSection(Long userId, int sectionOrderIndex, Long sectionId) {
        // --- ШАГ 1: ПРОВЕРКА ---
        // Прежде чем что-то менять, убеждаемся, что раздел действительно пройден.
        if (!isSectionCompleted(userId, sectionId)) {
            throw new IllegalStateException("Cannot complete a section before all its parts are completed.");
        }

        // --- ШАГ 2: ИЗМЕНЕНИЕ ---
        UserGlobalProgress progress = getGlobalProgress(userId);
        if (sectionOrderIndex > progress.getLastCompletedSectionOrderIndex()) {
            progress.setLastCompletedSectionOrderIndex(sectionOrderIndex);
            globalProgressRepository.save(progress);
        }

        // --- ШАГ 3: СБРОС КЭША ---
        try {
            courseServiceClient.clearOutlineCache(userId);
            log.info("Successfully requested cache invalidation for user {} after completing a section.", userId);
        } catch (Exception e) {
            log.error("Failed to request cache invalidation for user {} after completing a section.", userId, e);
        }
    }

    /**
     * Проверяет, выполнены ли все условия для завершения раздела:
     * 1. Пройдены все лекции.
     * 2. Пройдены все тесты глав.
     * 3. Пройден тест самого раздела.
     */
    private boolean isSectionCompleted(Long userId, Long sectionId) {
        // Получаем полную структуру раздела от course-service
        SectionForProgressDto section = courseServiceClient.getSectionStructure(sectionId);

        // 1. Проверяем лекции
        long totalLectures = section.getChapters().stream().mapToLong(c -> c.getLectures().size()).sum();
        long completedLectures = userProgressRepository.findByUserIdAndSectionId(userId, sectionId).size();
        if (totalLectures != completedLectures) {
            log.warn("User {} cannot complete section {}: not all lectures are completed.", userId, sectionId);
            return false;
        }

        // 2. Проверяем тесты глав
        for (var chapter : section.getChapters()) {
            if (chapter.getTestId() != null && !completedTestRepository.existsByUserIdAndTestId(userId, chapter.getTestId())) {
                log.warn("User {} cannot complete section {}: chapter test {} is not passed.", userId, sectionId, chapter.getTestId());
                return false;
            }
        }

        // 3. Проверяем тест раздела
        if (section.getTestId() != null && !completedTestRepository.existsByUserIdAndTestId(userId, section.getTestId())) {
            log.warn("User {} cannot complete section {}: section test {} is not passed.", userId, sectionId, section.getTestId());
            return false;
        }

        return true; // Все проверки пройдены
    }
}