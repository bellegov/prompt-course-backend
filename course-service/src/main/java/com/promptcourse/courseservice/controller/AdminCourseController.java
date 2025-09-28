package com.promptcourse.courseservice.controller;

import com.promptcourse.courseservice.dto.*;
import com.promptcourse.courseservice.model.*;
import com.promptcourse.courseservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin") // Все админские пути начинаются с /admin
@RequiredArgsConstructor
// Аннотация @PreAuthorize удалена, так как проверка ролей теперь на шлюзе
public class AdminCourseController {

    private final SectionRepository sectionRepository;
    private final ChapterRepository chapterRepository;
    private final LectureRepository lectureRepository;
    private final TestRepository testRepository;
    private final PromptRepository promptRepository;
    private final CacheManager cacheManager;
    private static final Logger log = LoggerFactory.getLogger(AdminCourseController.class);

    // === УПРАВЛЕНИЕ РАЗДЕЛАМИ ===

    @PostMapping("/sections")
    public ResponseEntity<Section> createSection(@RequestBody SectionDTO dto) {
        Section section = new Section();
        section.setTitle(dto.getTitle());
        section.setDescription(dto.getDescription());
        section.setOrderIndex(dto.getOrderIndex());
        section.setPremium(dto.isPremium());
        if (dto.getIconId() != null) {
            section.setIconId(dto.getIconId());
        }
        Section saved = sectionRepository.save(section);
        clearAllOutlineCaches();
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/sections")
    public ResponseEntity<List<Section>> getAllSections() {
        return ResponseEntity.ok(sectionRepository.findAll());
    }

    @GetMapping("/sections/{sectionId}")
    public ResponseEntity<Section> getSectionById(@PathVariable Long sectionId) {
        return sectionRepository.findById(sectionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<Section> updateSection(@PathVariable Long sectionId, @RequestBody SectionDTO dto) {
        clearAllOutlineCaches();
        return sectionRepository.findById(sectionId)
                .map(section -> {
                    section.setTitle(dto.getTitle());
                    section.setDescription(dto.getDescription());
                    section.setOrderIndex(dto.getOrderIndex());
                    section.setPremium(dto.isPremium());
                    if (dto.getIconId() != null) {
                        section.setIconId(dto.getIconId());
                    }

                    return ResponseEntity.ok(sectionRepository.save(section));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            return ResponseEntity.notFound().build();
        }
        sectionRepository.deleteById(sectionId);
        clearAllOutlineCaches();
        return ResponseEntity.noContent().build();
    }

    // === УПРАВЛЕНИЕ ГЛАВАМИ ===

    @PostMapping("/sections/{sectionId}/chapters")
    public ResponseEntity<Chapter> createChapter(@PathVariable Long sectionId, @RequestBody ChapterDTO dto) {
        clearAllOutlineCaches();
        return sectionRepository.findById(sectionId)
                .map(section -> {
                    Chapter chapter = new Chapter();
                    chapter.setTitle(dto.getTitle());
                    chapter.setOrderIndex(dto.getOrderIndex());
                    chapter.setSection(section);
                    return ResponseEntity.ok(chapterRepository.save(chapter));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sections/{sectionId}/chapters")
    public ResponseEntity<List<Chapter>> getChaptersBySection(@PathVariable Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chapterRepository.findBySectionId(sectionId));
    }

    @PutMapping("/chapters/{chapterId}")
    public ResponseEntity<Chapter> updateChapter(@PathVariable Long chapterId, @RequestBody ChapterDTO dto) {
        clearAllOutlineCaches();
        return chapterRepository.findById(chapterId)
                .map(chapter -> {
                    chapter.setTitle(dto.getTitle());
                    chapter.setOrderIndex(dto.getOrderIndex());
                    return ResponseEntity.ok(chapterRepository.save(chapter));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/chapters/{chapterId}")
    public ResponseEntity<Void> deleteChapter(@PathVariable Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            return ResponseEntity.notFound().build();
        }
        chapterRepository.deleteById(chapterId);
        clearAllOutlineCaches();
        return ResponseEntity.noContent().build();
    }

    // === УПРАВЛЕНИЕ ЛЕКЦИЯМИ ===

    @PostMapping("/chapters/{chapterId}/lectures")
    public ResponseEntity<Lecture> createLecture(@PathVariable Long chapterId, @RequestBody LectureDTO dto) {
        clearAllOutlineCaches();
        return chapterRepository.findById(chapterId)
                .map(chapter -> {
                    Lecture lecture = new Lecture();
                    lecture.setTitle(dto.getTitle());
                    lecture.setContentText(dto.getContentText());
                    lecture.setVideoUrl(dto.getVideoUrl());
                    lecture.setOrderIndex(dto.getOrderIndex());
                    lecture.setChapter(chapter);
                    return ResponseEntity.ok(lectureRepository.save(lecture));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/chapters/{chapterId}/lectures")
    public ResponseEntity<List<Lecture>> getLecturesByChapter(@PathVariable Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lectureRepository.findByChapterId(chapterId));
    }

    @PutMapping("/lectures/{lectureId}")
    public ResponseEntity<Lecture> updateLecture(@PathVariable Long lectureId, @RequestBody LectureDTO dto) {
        clearAllOutlineCaches();
        return lectureRepository.findById(lectureId)
                .map(lecture -> {
                    lecture.setTitle(dto.getTitle());
                    lecture.setContentText(dto.getContentText());
                    lecture.setVideoUrl(dto.getVideoUrl());
                    lecture.setOrderIndex(dto.getOrderIndex());
                    return ResponseEntity.ok(lectureRepository.save(lecture));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/lectures/{lectureId}")
    public ResponseEntity<Void> deleteLecture(@PathVariable Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            return ResponseEntity.notFound().build();
        }
        lectureRepository.deleteById(lectureId);
        clearAllOutlineCaches();
        return ResponseEntity.noContent().build();
    }

    // === УПРАВЛЕНИЕ ЗАПРОСАМИ (ПРОМПТАМИ) ===

    @PostMapping("/lectures/{lectureId}/prompts")
    public ResponseEntity<Prompt> addPromptToLecture(@PathVariable Long lectureId, @RequestBody PromptDto dto) {
        clearAllOutlineCaches();
        return lectureRepository.findById(lectureId)
                .map(lecture -> {
                    Prompt prompt = new Prompt();
                    prompt.setTitle(dto.getTitle());
                    prompt.setPromptText(dto.getPromptText());
                    prompt.setLecture(lecture);
                    return ResponseEntity.ok(promptRepository.save(prompt));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lectures/{lectureId}/prompts")
    public ResponseEntity<List<Prompt>> getPromptsForLecture(@PathVariable Long lectureId) {
        return lectureRepository.findById(lectureId)
                .map(lecture -> ResponseEntity.ok(lecture.getPrompts()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/prompts/{promptId}")
    public ResponseEntity<Prompt> updatePrompt(@PathVariable Long promptId, @RequestBody PromptDto dto) {
        clearAllOutlineCaches();
        return promptRepository.findById(promptId)
                .map(prompt -> {
                    prompt.setTitle(dto.getTitle());
                    prompt.setPromptText(dto.getPromptText());
                    return ResponseEntity.ok(promptRepository.save(prompt));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/prompts/{promptId}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long promptId) {
        if (!promptRepository.existsById(promptId)) {
            return ResponseEntity.notFound().build();
        }
        promptRepository.deleteById(promptId);
        clearAllOutlineCaches();
        return ResponseEntity.noContent().build();
    }
// === УПРАВЛЕНИЕ ТЕСТАМИ (НОВАЯ ВЕРСИЯ) ===

    /**
     * Создает или обновляет тест для конкретной лекции.
     * Если тест для лекции уже существует, он будет полностью заменен новыми данными.
     * Если не существует, будет создан новый.
     */
    @PostMapping("/lectures/{lectureId}/test")
    public ResponseEntity<Test> createOrUpdateTestForLecture(@PathVariable Long lectureId, @RequestBody CreateTestRequest request) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found with id: " + lectureId));

        Test test = testRepository.findByLectureId(lectureId).orElse(new Test());
        updateTestDataFromRequest(test, request);
        test.setLecture(lecture);
        test.setChapter(null); // Убедимся, что тест привязан только к одному элементу
        test.setSection(null);

        Test savedTest = testRepository.save(test);
        clearAllOutlineCaches();
        return ResponseEntity.ok(savedTest);
    }

    /**
     * Создает или обновляет тест для конкретной главы.
     */
    @PostMapping("/chapters/{chapterId}/test")
    public ResponseEntity<Test> createOrUpdateTestForChapter(@PathVariable Long chapterId, @RequestBody CreateTestRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found with id: " + chapterId));
        Test test = testRepository.findByChapterId(chapterId).orElse(new Test());
        updateTestDataFromRequest(test, request);
        test.setChapter(chapter);
        test.setLecture(null);
        test.setSection(null);

        Test savedTest = testRepository.save(test);
        clearAllOutlineCaches();
        return ResponseEntity.ok(savedTest);
    }

    /**
     * Создает или обновляет тест для конкретного раздела.
     */
    @PostMapping("/sections/{sectionId}/test")
    public ResponseEntity<Test> createOrUpdateTestForSection(@PathVariable Long sectionId, @RequestBody CreateTestRequest request) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));
        Test test = testRepository.findBySectionId(sectionId).orElse(new Test());
        updateTestDataFromRequest(test, request);
        test.setSection(section);
        test.setLecture(null);
        test.setChapter(null);

        Test savedTest = testRepository.save(test);
        clearAllOutlineCaches();
        return ResponseEntity.ok(savedTest);
    }

    /**
     * Получает существующий тест по его уникальному ID.
     * Необходимо фронтенду для заполнения формы редактирования.
     */
    @GetMapping("/tests/{testId}")
    public ResponseEntity<Test> getTestById(@PathVariable Long testId) {
        return testRepository.findById(testId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Удаляет тест по его уникальному ID.
     */
    @DeleteMapping("/tests/{testId}")
    public ResponseEntity<Void> deleteTest(@PathVariable Long testId) {
        if (!testRepository.existsById(testId)) {
            return ResponseEntity.notFound().build();
        }
        testRepository.deleteById(testId);
        clearAllOutlineCaches();
        return ResponseEntity.noContent().build();
    }

    // --- НОВЫЙ МЕТОД, ПРЕДЛОЖЕННЫЙ ФРОНТЕНДОМ ---
    /**
     * Находит и возвращает тест, привязанный к конкретной лекции.
     * Необходимо для фронтенда, чтобы получить testId для операций редактирования и удаления.
     */
    @GetMapping("/lectures/{lectureId}/test")
    public ResponseEntity<Test> getTestByLectureId(@PathVariable Long lectureId) {
        return testRepository.findByLectureId(lectureId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- И АНАЛОГИЧНЫЕ МЕТОДЫ ДЛЯ ГЛАВ И РАЗДЕЛОВ ---

    @GetMapping("/chapters/{chapterId}/test")
    public ResponseEntity<Test> getTestByChapterId(@PathVariable Long chapterId) {
        return testRepository.findByChapterId(chapterId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sections/{sectionId}/test")
    public ResponseEntity<Test> getTestBySectionId(@PathVariable Long sectionId) {
        return testRepository.findBySectionId(sectionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Приватный метод для обновления данных теста из DTO.
     * Вынесен, чтобы не дублировать код.
     */
    private void updateTestDataFromRequest(Test test, CreateTestRequest request) {
        test.setTitle(request.getTitle());

        if (request.getPassingScore() == null || request.getPassingScore() <= 0 || (request.getQuestions() != null && request.getPassingScore() > request.getQuestions().size())) {
            throw new IllegalArgumentException("Invalid passing score value. It must be between 1 and the number of questions.");
        }
        test.setPassingScore(request.getPassingScore());

        // Полностью очищаем старые вопросы, чтобы избежать дубликатов
        if (test.getQuestions() != null) {
            test.getQuestions().clear();
        } else {
            test.setQuestions(new ArrayList<>());
        }

        // Создаем новые вопросы на основе DTO
        if (request.getQuestions() != null) {
            request.getQuestions().forEach(qDto -> {
                Question question = new Question();
                question.setQuestionText(qDto.getQuestionText());
                question.setTest(test);
                question.setAnswers(new ArrayList<>());

                if (qDto.getAnswers() != null) {
                    qDto.getAnswers().forEach(aDto -> {
                        Answer answer = new Answer();
                        answer.setAnswerText(aDto.getAnswerText());
                        answer.setCorrect(aDto.getIsCorrect() != null && aDto.getIsCorrect());
                        answer.setQuestion(question);
                        question.getAnswers().add(answer);
                    });
                }
                test.getQuestions().add(question);
            });
        }
    }

    private void clearAllOutlineCaches() {
        try {
            // Находим кэш по имени и полностью его очищаем
            cacheManager.getCache("outlineCache").clear();
            log.info("--- Successfully cleared outlineCache ---");
        } catch (Exception e) {
            // Логируем ошибку, если кэш не найден или произошел сбой
            log.error("--- Failed to clear outlineCache ---", e);
        }
    }

}