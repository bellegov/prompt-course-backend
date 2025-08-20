package com.promptcourse.progressservice.service;
import com.promptcourse.progressservice.client.CourseServiceClient;
import com.promptcourse.progressservice.dto.*;
import com.promptcourse.progressservice.model.UserLifeStatus;
import com.promptcourse.progressservice.model.UserProgress;
import com.promptcourse.progressservice.repository.UserLifeStatusRepository;
import com.promptcourse.progressservice.repository.UserProgressRepository;
import com.promptcourse.progressservice.repository.CompletedTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final UserProgressRepository progressRepository;
    private final UserLifeStatusRepository lifeStatusRepository;
    private final CourseServiceClient courseServiceClient;
    private final CompletedTestRepository completedTestRepository;
    private final UserStatsService userStatsService;

    private static final int MAX_LIVES = 3;
    private static final int COOLDOWN_HOURS = 3;

    @Transactional // <-- Важная аннотация
    public void markLectureAsCompleted(Long userId, MarkCompletedRequest request) {
        // 1. Получаем инфо о лекции от course-service
        SectionForProgressDto.LectureForProgressDto lectureInfo = courseServiceClient.getLectureInfo(request.getLectureId());

        // 2. Если у лекции есть тест, проверяем, был ли он сдан
        if (lectureInfo.getTestId() != null) {
            Long testId = lectureInfo.getTestId();
            boolean testWasPassed = completedTestRepository.existsByUserIdAndTestId(userId, testId);
            if (!testWasPassed) {
                throw new IllegalStateException("Cannot complete lecture with an unpassed test. Test ID: " + testId);
            }
            // 3. Удаляем "временный пропуск" о сдаче теста
            completedTestRepository.findByUserIdAndTestId(userId, testId).ifPresent(completedTestRepository::delete);
        }

        // 4. Сохраняем прогресс по лекции
        if (!progressRepository.findByUserIdAndLectureIdIn(userId, Set.of(request.getLectureId())).isEmpty()) {
            return; // Лекция уже пройдена
        }

        UserProgress progress = UserProgress.builder()
                .userId(userId)
                .lectureId(request.getLectureId())
                .sectionId(request.getSectionId())
                .build();
        progressRepository.save(progress);
        userStatsService.incrementLecturesCompleted(userId);
    }

    public UserProgressResponse getUserProgress(Long userId, Long sectionId, boolean isSubscribed) {
        // --- БЛОК 1: ПОЛУЧАЕМ ИНФОРМАЦИЮ О ЖИЗНЯХ ---
        UserLifeStatus lifeStatus = getOrCreateLifeStatus(userId);

        if (!isSubscribed && lifeStatus.getRecoveryStartedAt() != null &&
                lifeStatus.getRecoveryStartedAt().plusHours(COOLDOWN_HOURS).isBefore(LocalDateTime.now())) {
            lifeStatus.setLives(MAX_LIVES);
            lifeStatus.setRecoveryStartedAt(null);
            lifeStatus = lifeStatusRepository.save(lifeStatus);
        }

        int currentLives = isSubscribed ? -1 : lifeStatus.getLives();
        String recoveryTimeLeft = calculateRecoveryTime(lifeStatus);

        // --- БЛОК 2: РАССЧИТЫВАЕМ ПРОГРЕСС ---

        // --- ЭТОТ КОД БЫЛ ПРОПУЩЕН ---
        SectionForProgressDto sectionStructure = courseServiceClient.getSectionStructure(sectionId);
        Set<Long> completedLectureIds = progressRepository.findByUserIdAndSectionId(userId, sectionId)
                .stream().map(UserProgress::getLectureId).collect(Collectors.toSet());
        // -----------------------------

        Map<Long, LectureState> lectureStates = new HashMap<>();
        Map<Long, Boolean> chapterStates = new HashMap<>();
        boolean previousChapterCompleted = true;

        for (SectionForProgressDto.ChapterForProgressDto chapterDto : sectionStructure.getChapters()) {
            boolean isChapterUnlocked = previousChapterCompleted;
            chapterStates.put(chapterDto.getChapterId(), isChapterUnlocked);

            boolean allLecturesInChapterCompleted = true;

            List<Long> orderedLectureIds = chapterDto.getLectures().stream()
                    .map(SectionForProgressDto.LectureForProgressDto::getLectureId)
                    .collect(Collectors.toList());

            for(Long lectureId : orderedLectureIds) {
                if(completedLectureIds.contains(lectureId)) {
                    lectureStates.put(lectureId, LectureState.COMPLETED);
                } else {
                    lectureStates.put(lectureId, LectureState.LOCKED);
                }
            }

            if (isChapterUnlocked) {
                for (Long lectureId : orderedLectureIds) {
                    if (lectureStates.get(lectureId) == LectureState.LOCKED) {
                        lectureStates.put(lectureId, LectureState.UNLOCKED);
                        break;
                    }
                }
            }

            for(Long lectureId : orderedLectureIds) {
                if (lectureStates.get(lectureId) != LectureState.COMPLETED) {
                    allLecturesInChapterCompleted = false;
                    break;
                }
            }

            if (!allLecturesInChapterCompleted) {
                previousChapterCompleted = false;
            }
        }

        long totalLectures = sectionStructure.getChapters().stream().mapToLong(c -> c.getLectures().size()).sum();
        int progressPercentage = totalLectures > 0 ? (int) (((double) completedLectureIds.size() / totalLectures) * 100) : 0;

        // --- БЛОК 3: ФОРМИРУЕМ ОТВЕТ ---
        return UserProgressResponse.builder()
                .lectureStates(lectureStates)
                .chapterStates(chapterStates)
                .progressPercentage(progressPercentage)
                .livesLeft(currentLives)
                .recoveryTimeLeft(recoveryTimeLeft)
                .build();
    }

    private UserLifeStatus getOrCreateLifeStatus(Long userId) {
        return lifeStatusRepository.findByUserId(userId)
                .orElseGet(() -> lifeStatusRepository.save(
                        UserLifeStatus.builder().userId(userId).lives(MAX_LIVES).build()
                ));
    }

    private String calculateRecoveryTime(UserLifeStatus lifeStatus) {
        if (lifeStatus.getLives() >= MAX_LIVES || lifeStatus.getRecoveryStartedAt() == null) {
            return "00:00:00";
        }
        LocalDateTime recoveryTime = lifeStatus.getRecoveryStartedAt().plusHours(COOLDOWN_HOURS);
        Duration duration = Duration.between(LocalDateTime.now(), recoveryTime);
        if (duration.isNegative()) {
            return "00:00:00";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    public Set<Long> getCompletedLectureIds(Long userId) {
        return progressRepository.findByUserId(userId).stream() // <-- Нам нужен этот метод в репозитории
                .map(UserProgress::getLectureId)
                .collect(Collectors.toSet());
    }

    }