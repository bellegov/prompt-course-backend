package com.promptcourse.progressservice;
import com.promptcourse.progressservice.client.CourseServiceClient;
import com.promptcourse.progressservice.dto.LectureState;
import com.promptcourse.progressservice.dto.SectionForProgressDto;
import com.promptcourse.progressservice.dto.UserProgressResponse;
import com.promptcourse.progressservice.model.UserLifeStatus;
import com.promptcourse.progressservice.model.UserProgress;
import com.promptcourse.progressservice.repository.UserLifeStatusRepository;
import com.promptcourse.progressservice.repository.UserProgressRepository;
import com.promptcourse.progressservice.service.ProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private UserProgressRepository progressRepository;
    @Mock
    private CourseServiceClient courseServiceClient;
    @Mock // <-- ДОБАВЛЯЕМ НЕДОСТАЮЩУЮ ЗАГЛУШКУ
    private UserLifeStatusRepository userLifeStatusRepository;
    // UserStatsService нам по-прежнему не нужен для этого конкретного теста

    @InjectMocks
    private ProgressService progressService;

    private SectionForProgressDto mockCourseStructure;

    // Метод @BeforeEach выполняется перед КАЖДЫМ тестом
    @BeforeEach
    void setUp() {
        // Создаем "фальшивую" структуру курса один раз, чтобы использовать в тестах

        // Глава 1 с двумя лекциями (ID 10, 11)
        var chapter1 = SectionForProgressDto.ChapterForProgressDto.builder()
                .chapterId(1L)
                .lectures(List.of(
                        new SectionForProgressDto.LectureForProgressDto(10L, 101L), // lectureId, testId
                        new SectionForProgressDto.LectureForProgressDto(11L, null)
                ))
                .build();

        // Глава 2 с одной лекцией (ID 20)
        var chapter2 = SectionForProgressDto.ChapterForProgressDto.builder()
                .chapterId(2L)
                .lectures(List.of(
                        new SectionForProgressDto.LectureForProgressDto(20L, 102L)
                ))
                .build();

        mockCourseStructure = SectionForProgressDto.builder()
                .sectionId(100L)
                .chapters(List.of(chapter1, chapter2))
                .build();

        when(userLifeStatusRepository.findByUserId(any(Long.class)))
                .thenReturn(Optional.empty());
        // Также обучим его "сохранять"
        when(userLifeStatusRepository.save(any(UserLifeStatus.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }


    @Test
    void getUserProgress_WhenUserHasNoProgress_ShouldUnlockFirstLectureOnly() {
        // --- 1. ARRANGE (Подготовка) ---
        Long userId = 1L;
        Long sectionId = 100L;

        // "Обучаем" заглушки:
        // Когда Feign-клиента попросят структуру для sectionId=100, он вернет нашу фальшивую структуру
        when(courseServiceClient.getSectionStructure(sectionId)).thenReturn(mockCourseStructure);

        // Когда репозиторий попросят найти прогресс для userId=1, он вернет ПУСТОЙ список
        when(progressRepository.findByUserIdAndSectionId(userId, sectionId)).thenReturn(List.of());

        // --- 2. ACT (Действие) ---
        UserProgressResponse response = progressService.getUserProgress(userId, sectionId, false);

        // --- 3. ASSERT (Проверка) ---
        assertNotNull(response);

        // Проверяем статусы лекций
        assertEquals(LectureState.UNLOCKED, response.getLectureStates().get(10L)); // Первая лекция должна быть активна
        assertEquals(LectureState.LOCKED, response.getLectureStates().get(11L));
        assertEquals(LectureState.LOCKED, response.getLectureStates().get(20L));

        // Проверяем статусы глав
        assertTrue(response.getChapterStates().get(1L)); // Первая глава доступна
        assertFalse(response.getChapterStates().get(2L)); // Вторая заблокирована

        assertEquals(0, response.getProgressPercentage());
    }

    @Test
    void getUserProgress_WhenFirstLectureCompleted_ShouldUnlockSecondLecture() {
        // --- 1. ARRANGE (Подготовка) ---
        Long userId = 2L;
        Long sectionId = 100L;

        // Симулируем, что пользователь прошел первую лекцию (ID 10)
        UserProgress completedProgress = UserProgress.builder().userId(userId).lectureId(10L).sectionId(sectionId).build();

        when(courseServiceClient.getSectionStructure(sectionId)).thenReturn(mockCourseStructure);
        when(progressRepository.findByUserIdAndSectionId(userId, sectionId)).thenReturn(List.of(completedProgress));

        // --- 2. ACT (Действие) ---
        UserProgressResponse response = progressService.getUserProgress(userId, sectionId, false);

        // --- 3. ASSERT (Проверка) ---
        assertEquals(LectureState.COMPLETED, response.getLectureStates().get(10L)); // Первая пройдена
        assertEquals(LectureState.UNLOCKED, response.getLectureStates().get(11L));  // Вторая стала активной
        assertEquals(LectureState.LOCKED, response.getLectureStates().get(20L));

        assertTrue(response.getChapterStates().get(1L));
        assertFalse(response.getChapterStates().get(2L)); // Вторая глава все еще заблокирована
    }

    @Test
    void getUserProgress_WhenFirstChapterCompleted_ShouldUnlockSecondChapter() {
        // --- 1. ARRANGE (Подготовка) ---
        Long userId = 3L;
        Long sectionId = 100L;

        // Симулируем, что пользователь прошел ВСЕ лекции в первой главе (ID 10 и 11)
        List<UserProgress> completedProgress = List.of(
                UserProgress.builder().userId(userId).lectureId(10L).sectionId(sectionId).build(),
                UserProgress.builder().userId(userId).lectureId(11L).sectionId(sectionId).build()
        );

        when(courseServiceClient.getSectionStructure(sectionId)).thenReturn(mockCourseStructure);
        when(progressRepository.findByUserIdAndSectionId(userId, sectionId)).thenReturn(completedProgress);

        // --- 2. ACT (Действие) ---
        UserProgressResponse response = progressService.getUserProgress(userId, sectionId, false);

        // --- 3. ASSERT (Проверка) ---
        assertEquals(LectureState.COMPLETED, response.getLectureStates().get(10L));
        assertEquals(LectureState.COMPLETED, response.getLectureStates().get(11L));
        assertEquals(LectureState.UNLOCKED, response.getLectureStates().get(20L)); // Первая лекция второй главы стала активной

        assertTrue(response.getChapterStates().get(1L)); // Первая глава доступна (и пройдена)
        assertTrue(response.getChapterStates().get(2L)); // Вторая глава РАЗБЛОКИРОВАЛАСЬ
    }
}
