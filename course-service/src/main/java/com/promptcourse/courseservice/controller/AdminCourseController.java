package com.promptcourse.courseservice.controller;

import com.promptcourse.courseservice.dto.*;
import com.promptcourse.courseservice.model.*;
import com.promptcourse.courseservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
// Путь, который настроен в шлюзе
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    private final SectionRepository sectionRepository;
    private final ChapterRepository chapterRepository;
    private final LectureRepository lectureRepository;
    private final TestRepository testRepository;
    private final PromptRepository promptRepository;

    // === УПРАВЛЕНИЕ РАЗДЕЛАМИ ===
    // CREATE
    @PostMapping("/sections")
    public ResponseEntity<Section> createSection(@RequestBody SectionDTO dto) {
        Section section = new Section();
        section.setTitle(dto.getTitle());
        section.setDescription(dto.getDescription());
        section.setOrderIndex(dto.getOrderIndex());
        Section saved = sectionRepository.save(section);
        return ResponseEntity.ok(saved);
    }
    // READ (Получить все разделы)
    @GetMapping("/sections")
    public ResponseEntity<List<Section>> getAllSections() {
        List<Section> sections = sectionRepository.findAll();
        return ResponseEntity.ok(sections);
    }

    // READ (Получить один раздел по ID)
    @GetMapping("/sections/{sectionId}")
    public ResponseEntity<Section> getSectionById(@PathVariable Long sectionId) {
        return sectionRepository.findById(sectionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE (Обновить раздел)
    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<Section> updateSection(@PathVariable Long sectionId, @RequestBody SectionDTO dto) {
        return sectionRepository.findById(sectionId)
                .map(section -> {
                    section.setTitle(dto.getTitle());
                    section.setDescription(dto.getDescription());
                    section.setOrderIndex(dto.getOrderIndex());
                    Section updatedSection = sectionRepository.save(section);
                    return ResponseEntity.ok(updatedSection);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE (Удалить раздел)
    @DeleteMapping("/sections/{sectionId}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            return ResponseEntity.notFound().build();
        }
        sectionRepository.deleteById(sectionId);
        return ResponseEntity.noContent().build(); // Стандартный ответ "Успешно удалено, нет содержимого"
    }

    // === УПРАВЛЕНИЕ ГЛАВАМИ ===

    @PostMapping("/sections/{sectionId}/chapters")
    public ResponseEntity<Chapter> createChapter(@PathVariable Long sectionId, @RequestBody ChapterDTO dto) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section with id " + sectionId + " not found"));
        Chapter chapter = new Chapter();
        chapter.setTitle(dto.getTitle());
        chapter.setOrderIndex(dto.getOrderIndex());
        chapter.setSection(section);
        Chapter saved = chapterRepository.save(chapter);
        return ResponseEntity.ok(saved);
    }
    // READ (Получить все главы конкретного раздела) - это полезный метод
    @GetMapping("/sections/{sectionId}/chapters")
    public ResponseEntity<List<Chapter>> getChaptersBySection(@PathVariable Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            return ResponseEntity.notFound().build();
        }
        List<Chapter> chapters = chapterRepository.findBySectionId(sectionId); // Такой метод нужно будет добавить в репозиторий
        return ResponseEntity.ok(chapters);
    }

    // UPDATE (Обновить главу)
    @PutMapping("/chapters/{chapterId}") // Обновляем по ID самой главы
    public ResponseEntity<Chapter> updateChapter(@PathVariable Long chapterId, @RequestBody ChapterDTO dto) {
        return chapterRepository.findById(chapterId)
                .map(chapter -> {
                    chapter.setTitle(dto.getTitle());
                    chapter.setOrderIndex(dto.getOrderIndex());
                    Chapter updatedChapter = chapterRepository.save(chapter);
                    return ResponseEntity.ok(updatedChapter);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE (Удалить главу)
    @DeleteMapping("/chapters/{chapterId}")
    public ResponseEntity<Void> deleteChapter(@PathVariable Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            return ResponseEntity.notFound().build();
        }
        chapterRepository.deleteById(chapterId);
        return ResponseEntity.noContent().build();
    }

    // === УПРАВЛЕНИЕ ЛЕКЦИЯМИ ===

    @PostMapping("/chapters/{chapterId}/lectures")
    public ResponseEntity<Lecture> createLecture(@PathVariable Long chapterId, @RequestBody LectureDTO dto) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter with id " + chapterId + " not found"));
        Lecture lecture = new Lecture();
        lecture.setTitle(dto.getTitle());
        lecture.setContentText(dto.getContentText());
        lecture.setVideoUrl(dto.getVideoUrl());
        lecture.setOrderIndex(dto.getOrderIndex());
        lecture.setChapter(chapter);
        Lecture saved = lectureRepository.save(lecture);
        return ResponseEntity.ok(saved);
    }
    // READ (Получить все лекции конкретной главы)
    @GetMapping("/chapters/{chapterId}/lectures")
    public ResponseEntity<List<Lecture>> getLecturesByChapter(@PathVariable Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            return ResponseEntity.notFound().build();
        }
        List<Lecture> lectures = lectureRepository.findByChapterId(chapterId); // <-- Добавить метод в репозиторий
        return ResponseEntity.ok(lectures);
    }

    // UPDATE (Обновить лекцию)
    @PutMapping("/lectures/{lectureId}")
    public ResponseEntity<Lecture> updateLecture(@PathVariable Long lectureId, @RequestBody LectureDTO dto) {
        return lectureRepository.findById(lectureId)
                .map(lecture -> {
                    lecture.setTitle(dto.getTitle());
                    lecture.setContentText(dto.getContentText());
                    lecture.setVideoUrl(dto.getVideoUrl());
                    lecture.setOrderIndex(dto.getOrderIndex());
                    Lecture updatedLecture = lectureRepository.save(lecture);
                    return ResponseEntity.ok(updatedLecture);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE (Удалить лекцию)
    @DeleteMapping("/lectures/{lectureId}")
    public ResponseEntity<Void> deleteLecture(@PathVariable Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            return ResponseEntity.notFound().build();
        }
        lectureRepository.deleteById(lectureId);
        return ResponseEntity.noContent().build();
    }

    // === УПРАВЛЕНИЕ ТЕСТАМИ ===
    @PostMapping("/lectures/{lectureId}/test")
    public ResponseEntity<Test> createTestForLecture(@PathVariable Long lectureId, @RequestBody CreateTestRequest request) {
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new RuntimeException("Lecture not found"));
        Test test = buildTestFromRequest(request);
        test.setLecture(lecture);
        return ResponseEntity.ok(testRepository.save(test));
    }

    @PostMapping("/chapters/{chapterId}/test")
    public ResponseEntity<Test> createTestForChapter(@PathVariable Long chapterId, @RequestBody CreateTestRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId).orElseThrow(() -> new RuntimeException("Chapter not found"));
        Test test = buildTestFromRequest(request);
        test.setChapter(chapter);
        return ResponseEntity.ok(testRepository.save(test));
    }

    @PostMapping("/sections/{sectionId}/test")
    public ResponseEntity<Test> createTestForSection(@PathVariable Long sectionId, @RequestBody CreateTestRequest request) {
        Section section = sectionRepository.findById(sectionId).orElseThrow(() -> new RuntimeException("Section not found"));
        Test test = buildTestFromRequest(request);
        test.setSection(section);
        return ResponseEntity.ok(testRepository.save(test));
    }

    @GetMapping("/tests/{testId}")
    public ResponseEntity<Test> getTestById(@PathVariable Long testId) {
        return testRepository.findById(testId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/tests/{testId}")
    public ResponseEntity<Test> updateTest(@PathVariable Long testId, @RequestBody CreateTestRequest request) {
        return testRepository.findById(testId)
                .map(existingTest -> {
                    if (request.getPassingScore() > request.getQuestions().size() || request.getPassingScore() <= 0) {
                        throw new IllegalArgumentException("Invalid passing score value.");
                    }
                    existingTest.setTitle(request.getTitle());
                    existingTest.setPassingScore(request.getPassingScore());
                    existingTest.getQuestions().clear();
                    Test updatedTestModel = buildTestFromRequest(request);
                    updatedTestModel.getQuestions().forEach(q -> {
                        q.setTest(existingTest);
                        existingTest.getQuestions().add(q);
                    });
                    return ResponseEntity.ok(testRepository.save(existingTest));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/tests/{testId}")
    public ResponseEntity<Void> deleteTest(@PathVariable Long testId) {
        if (!testRepository.existsById(testId)) return ResponseEntity.notFound().build();
        testRepository.deleteById(testId);
        return ResponseEntity.noContent().build();
    }

    private Test buildTestFromRequest(CreateTestRequest request) {
        Test test = new Test();
        test.setTitle(request.getTitle());
        if (request.getPassingScore() > request.getQuestions().size() || request.getPassingScore() <= 0) {
            throw new IllegalArgumentException("Invalid passing score value.");
        }
        test.setPassingScore(request.getPassingScore());
        test.setQuestions(new ArrayList<>());
        request.getQuestions().forEach(qDto -> {
            Question question = new Question();
            question.setQuestionText(qDto.getQuestionText());
            question.setTest(test);
            question.setAnswers(new ArrayList<>());
            qDto.getAnswers().forEach(aDto -> {
                Answer answer = new Answer();
                answer.setAnswerText(aDto.getAnswerText());
                answer.setCorrect(aDto.getIsCorrect() != null && aDto.getIsCorrect());
                answer.setQuestion(question);
                question.getAnswers().add(answer);
            });
            test.getQuestions().add(question);
        });
        return test;
    }
    // === УПРАВЛЕНИЕ ЗАПРОСАМИ (ПРОМПТАМИ) ===

    // CREATE (Добавить промпт к лекции)
    @PostMapping("/lectures/{lectureId}/prompts")
    public ResponseEntity<Prompt> addPromptToLecture(@PathVariable Long lectureId, @RequestBody PromptDto dto) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found with id: " + lectureId));
        Prompt prompt = new Prompt();
        prompt.setTitle(dto.getTitle());
        prompt.setPromptText(dto.getPromptText());
        prompt.setLecture(lecture);
        return ResponseEntity.ok(promptRepository.save(prompt));
    }

    // READ (Получить все промпты для лекции)
    @GetMapping("/lectures/{lectureId}/prompts")
    public ResponseEntity<List<Prompt>> getPromptsForLecture(@PathVariable Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found with id: " + lectureId));
        return ResponseEntity.ok(lecture.getPrompts());
    }

    // UPDATE (Обновить текст промпта)
    @PutMapping("/prompts/{promptId}")
    public ResponseEntity<Prompt> updatePrompt(@PathVariable Long promptId, @RequestBody PromptDto dto) {
        return promptRepository.findById(promptId)
                .map(prompt -> {
                    prompt.setTitle(dto.getTitle());
                    prompt.setPromptText(dto.getPromptText());
                    return ResponseEntity.ok(promptRepository.save(prompt));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE (Удалить промпт)
    @DeleteMapping("/prompts/{promptId}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long promptId) {
        if (!promptRepository.existsById(promptId)) {
            return ResponseEntity.notFound().build();
        }
        promptRepository.deleteById(promptId);
        return ResponseEntity.noContent().build();
    }
}