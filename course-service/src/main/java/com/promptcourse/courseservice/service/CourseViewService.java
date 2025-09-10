package com.promptcourse.courseservice.service;

import com.promptcourse.courseservice.client.ProgressServiceClient;
import com.promptcourse.courseservice.dto.CourseOutlineDto;
import com.promptcourse.courseservice.dto.CourseOutlineDto.*;
import com.promptcourse.courseservice.dto.LectureContentDto;
import com.promptcourse.courseservice.dto.LectureState;
import com.promptcourse.courseservice.dto.progress.ProgressRequest;
import com.promptcourse.courseservice.dto.progress.UserGlobalProgress;
import com.promptcourse.courseservice.dto.progress.UserProgressResponse;
import com.promptcourse.courseservice.model.*;
import com.promptcourse.courseservice.repository.LectureRepository;
import com.promptcourse.courseservice.repository.SectionRepository;
import com.promptcourse.courseservice.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseViewService {

    private final SectionRepository sectionRepository;
    private final LectureRepository lectureRepository;
    private final TestRepository testRepository;
    private final ProgressServiceClient progressServiceClient;

    // --- ДОБАВЛЯЕМ АННОТАЦИЮ ---
    // "outlineCache" - это имя нашего кэша
    // key = "#userId" - ключ, по которому будут сохраняться данные (ID пользователя)
    @Cacheable(value = "outlineCache", key = "#userId + '-' + #isSubscribed")
    public CourseOutlineDto getCourseOutline(Long userId, boolean isSubscribed) {
        log.info(">>> [CACHE MISS] Calculating outline for userId: {}, isSubscribed: {}", userId, isSubscribed);
        // 1. Получаем "чистую" структуру курса, отсортированную по orderIndex
        List<Section> sections = sectionRepository.findAllByOrderByOrderIndexAsc();

        // 2. Получаем глобальный прогресс пользователя ОДИН РАЗ
        UserGlobalProgress globalProgress = progressServiceClient.getGlobalProgress(userId);
        int lastCompletedIndex = globalProgress.getLastCompletedSectionOrderIndex();

        // 3. Формируем список секций с учетом статуса подписки и прогресса
        List<SectionOutlineDto> sectionDtos = sections.stream()
                .map(section -> {
                    // --- ЛОГИКА РАЗБЛОКИРОВКИ РАЗДЕЛОВ ---
                    boolean isSectionUnlocked;
                    if (isSubscribed) {
                        isSectionUnlocked = true;
                    } else {
                        isSectionUnlocked = section.getOrderIndex() <= (lastCompletedIndex + 1);
                    }

                    // Запрашиваем детальный прогресс только для разблокированных секций
                    UserProgressResponse progress = isSectionUnlocked
                            ? progressServiceClient.getProgressForUser(new ProgressRequest(userId, section.getId(), isSubscribed))
                            : null;

                    return mapSectionToDto(section, progress, isSectionUnlocked);
                })
                .collect(Collectors.toList());

        // 4. Получаем данные о жизнях и считаем общий прогресс
        int livesLeft = 3;
        String recoveryTimeLeft = "00:00:00";
        if (!sections.isEmpty()) {
            UserProgressResponse livesProgress = progressServiceClient.getProgressForUser(
                    new ProgressRequest(userId, sections.get(0).getId(), isSubscribed)
            );
            livesLeft = livesProgress.getLivesLeft();
            recoveryTimeLeft = livesProgress.getRecoveryTimeLeft();
        }

        int totalCourseProgress = calculateTotalProgress(sectionDtos);

        return CourseOutlineDto.builder()
                .sections(sectionDtos)
                .livesLeft(livesLeft)
                .recoveryTimeLeft(recoveryTimeLeft)
                .totalCourseProgress(totalCourseProgress)
                .build();
    }
    @CacheEvict(value = "outlineCache", key = "#userId + '-true'")
    public void clearSubscriberCache(Long userId) {
        // --- ЛОГ №2 ---
        log.info(">>> [CACHE EVICT] Clearing SUBSCRIBER cache for userId: {}", userId);
    }

    @CacheEvict(value = "outlineCache", key = "#userId + '-false'")
    public void clearNonSubscriberCache(Long userId) {
        // --- ЛОГ №3 ---
        log.info(">>> [CACHE EVICT] Clearing NON-SUBSCRIBER cache for userId: {}", userId);
    }

    public LectureContentDto getLectureContent(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));
        Long testId = testRepository.findByLectureId(lectureId).map(Test::getId).orElse(null);
        return LectureContentDto.builder()
                .id(lecture.getId()).title(lecture.getTitle()).contentText(lecture.getContentText())
                .videoUrl(lecture.getVideoUrl()).testId(testId).build();
    }

    private SectionOutlineDto mapSectionToDto(Section section, UserProgressResponse progress, boolean isSectionUnlocked) {
        Long testId = testRepository.findBySectionId(section.getId()).map(Test::getId).orElse(null);

        // Если прогресс null (для заблокированных секций), создаем пустой объект, чтобы избежать NPE
        UserProgressResponse safeProgress = (progress != null) ? progress : UserProgressResponse.builder().build();

        Map<Long, LectureState> convertedLectureStates = safeProgress.getLectureStates() != null
                ? safeProgress.getLectureStates().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> LectureState.valueOf(e.getValue().name())))
                : Map.of();

        return SectionOutlineDto.builder()
                .id(section.getId()).title(section.getTitle()).description(section.getDescription())
                .chapters(section.getChapters().stream()
                        .sorted(Comparator.comparingInt(Chapter::getOrderIndex))
                        .map(chapter -> mapChapterToDto(chapter, safeProgress.getChapterStates(), convertedLectureStates))
                        .collect(Collectors.toList()))
                .testId(testId)
                .progressPercentage(safeProgress.getProgressPercentage())
                .isUnlocked(isSectionUnlocked)
                .iconId(section.getIconId())
                .build();
    }

    private ChapterOutlineDto mapChapterToDto(Chapter chapter, Map<Long, Boolean> chapterStates, Map<Long, LectureState> lectureStates) {
        Long testId = testRepository.findByChapterId(chapter.getId()).map(Test::getId).orElse(null);
        List<Lecture> sortedLectures = chapter.getLectures().stream()
                .sorted(Comparator.comparingInt(Lecture::getOrderIndex)).collect(Collectors.toList());
        List<LectureOutlineDto> lectureDtos = new ArrayList<>();

        for (int i = 0; i < sortedLectures.size(); i++) {
            Lecture currentLecture = sortedLectures.get(i);
            boolean promptsAvailable = false;
            if (i + 1 < sortedLectures.size()) {
                Lecture nextLecture = sortedLectures.get(i + 1);
                LectureState nextState = lectureStates.get(nextLecture.getId());
                if (nextState != null && nextState != LectureState.LOCKED) promptsAvailable = true;
            } else {
                promptsAvailable = true;
            }
            lectureDtos.add(mapLectureToDto(currentLecture, lectureStates, promptsAvailable));
        }
        return ChapterOutlineDto.builder()
                .id(chapter.getId()).title(chapter.getTitle()).lectures(lectureDtos)
                .testId(testId)
                .isUnlocked(chapterStates != null && chapterStates.getOrDefault(chapter.getId(), false))
                .build();
    }

    private LectureOutlineDto mapLectureToDto(Lecture lecture, Map<Long, LectureState> lectureStates, boolean promptsAvailable) {
        Long testId = testRepository.findByLectureId(lecture.getId()).map(Test::getId).orElse(null);
        return LectureOutlineDto.builder()
                .id(lecture.getId()).title(lecture.getTitle()).testId(testId)
                .state(lectureStates.getOrDefault(lecture.getId(), LectureState.LOCKED))
                .promptsAvailable(promptsAvailable).build();
    }

    private int calculateTotalProgress(List<SectionOutlineDto> sectionDtos) {
        long totalLecturesInCourse = sectionDtos.stream()
                .flatMap(s -> s.getChapters().stream())
                .mapToLong(c -> c.getLectures().size()).sum();

        long completedLecturesInCourse = sectionDtos.stream()
                .flatMap(s -> s.getChapters().stream())
                .flatMap(c -> c.getLectures().stream())
                .filter(l -> l.getState() == LectureState.COMPLETED).count();

        return (totalLecturesInCourse > 0)
                ? (int) (((double) completedLecturesInCourse / totalLecturesInCourse) * 100)
                : 0;
    }
}