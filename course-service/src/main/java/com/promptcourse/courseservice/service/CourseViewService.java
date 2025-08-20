package com.promptcourse.courseservice.service;

import com.promptcourse.courseservice.client.ProgressServiceClient;
import com.promptcourse.courseservice.dto.CourseOutlineDto;
import com.promptcourse.courseservice.dto.CourseOutlineDto.*;
import com.promptcourse.courseservice.dto.LectureContentDto;
import com.promptcourse.courseservice.dto.LectureState; // Наш "родной" DTO
import com.promptcourse.courseservice.dto.progress.ProgressRequest;
import com.promptcourse.courseservice.dto.progress.UserProgressResponse;
import com.promptcourse.courseservice.model.*;
import com.promptcourse.courseservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseViewService {

    private final SectionRepository sectionRepository;
    private final LectureRepository lectureRepository;
    private final TestRepository testRepository;
    private final ProgressServiceClient progressServiceClient;

    public CourseOutlineDto getCourseOutline(Long userId, boolean isSubscribed) {
        List<Section> sections = sectionRepository.findAllByOrderByOrderIndexAsc();

        // Создаем пустой DTO для ответа
        CourseOutlineDto responseDto = CourseOutlineDto.builder()
                .livesLeft(3) // Значения по умолчанию
                .recoveryTimeLeft("00:00:00")
                .build();

        // Переменные для расчета общего прогресса
        long totalLecturesInCourse = 0;
        long completedLecturesInCourse = 0;

        List<SectionOutlineDto> sectionDtos = sections.stream()
                .map(section -> {
                    // --- ПРОСТОЙ И НАДЕЖНЫЙ СПОСОБ СОЗДАТЬ ЗАПРОС ---
                    ProgressRequest progressRequest = new ProgressRequest();
                    progressRequest.setUserId(userId);
                    progressRequest.setSectionId(section.getId());
                    progressRequest.setSubscribed(isSubscribed);
                    // ---------------------------------------------

                    UserProgressResponse progress = progressServiceClient.getProgressForUser(progressRequest);

                    // Обновляем общие данные о жизнях из самого первого ответа
                    if (responseDto.getRecoveryTimeLeft().equals("00:00:00")) {
                        responseDto.setLivesLeft(progress.getLivesLeft());
                        responseDto.setRecoveryTimeLeft(progress.getRecoveryTimeLeft());
                    }

                    return mapSectionToDto(section, progress);
                })
                .collect(Collectors.toList());

        // Рассчитываем общий прогресс после получения всех данных
        for (SectionOutlineDto sectionDto : sectionDtos) {
            long totalLecturesInSection = sectionDto.getChapters().stream()
                    .mapToLong(c -> c.getLectures().size()).sum();
            totalLecturesInCourse += totalLecturesInSection;
            completedLecturesInCourse += (totalLecturesInSection * sectionDto.getProgressPercentage()) / 100.0;
        }

        int totalCourseProgress = (totalLecturesInCourse > 0)
                ? (int) (((double) completedLecturesInCourse / totalLecturesInCourse) * 100)
                : 0;

        responseDto.setSections(sectionDtos);
        responseDto.setTotalCourseProgress(totalCourseProgress);

        return responseDto;
    }

    public LectureContentDto getLectureContent(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));
        Long testId = testRepository.findByLectureId(lectureId).map(Test::getId).orElse(null);
        return LectureContentDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .contentText(lecture.getContentText())
                .videoUrl(lecture.getVideoUrl())
                .testId(testId)
                .build();
    }

    private SectionOutlineDto mapSectionToDto(Section section, UserProgressResponse progress) {
        Long testId = testRepository.findBySectionId(section.getId()).map(Test::getId).orElse(null);

        // --- ИСПРАВЛЕНИЕ: Конвертируем карту перед передачей ---
        Map<Long, LectureState> convertedLectureStates = progress.getLectureStates().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> LectureState.valueOf(entry.getValue().name()) // Преобразуем Enum
                ));

        return SectionOutlineDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .chapters(section.getChapters().stream()
                        .sorted(Comparator.comparingInt(Chapter::getOrderIndex))
                        .map(chapter -> mapChapterToDto(chapter, progress.getChapterStates(), convertedLectureStates)) // Передаем уже конвертированную карту
                        .collect(Collectors.toList()))
                .testId(testId)
                .progressPercentage(progress.getProgressPercentage())
                .build();
    }

    private ChapterOutlineDto mapChapterToDto(Chapter chapter, Map<Long, Boolean> chapterStates, Map<Long, LectureState> lectureStates) {
        Long testId = testRepository.findByChapterId(chapter.getId()).map(Test::getId).orElse(null);

        List<Lecture> sortedLectures = chapter.getLectures().stream()
                .sorted(Comparator.comparingInt(Lecture::getOrderIndex))
                .collect(Collectors.toList());

        List<LectureOutlineDto> lectureDtos = new ArrayList<>();

        for (int i = 0; i < sortedLectures.size(); i++) {
            Lecture currentLecture = sortedLectures.get(i);
            boolean promptsAvailableForCurrent = false;

            if (i + 1 < sortedLectures.size()) {
                Lecture nextLecture = sortedLectures.get(i + 1);
                LectureState nextLectureState = lectureStates.get(nextLecture.getId());
                if (nextLectureState != null && nextLectureState != LectureState.LOCKED) {
                    promptsAvailableForCurrent = true;
                }
            } else {
                // Это последняя лекция в последней главе. Промпты всегда доступны.
                promptsAvailableForCurrent = true;
            }
            lectureDtos.add(mapLectureToDto(currentLecture, lectureStates, promptsAvailableForCurrent));
        }

        return ChapterOutlineDto.builder()
                .id(chapter.getId())
                .title(chapter.getTitle())
                .lectures(lectureDtos)
                .testId(testId)
                .isUnlocked(chapterStates.getOrDefault(chapter.getId(), false))
                .build();
    }

    private LectureOutlineDto mapLectureToDto(Lecture lecture, Map<Long, LectureState> lectureStates, boolean promptsAvailable) {
        Long testId = testRepository.findByLectureId(lecture.getId()).map(Test::getId).orElse(null);
        return LectureOutlineDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .testId(testId)
                .state(lectureStates.getOrDefault(lecture.getId(), LectureState.LOCKED))
                .promptsAvailable(promptsAvailable)
                .build();
    }
}

