package com.promptcourse.courseservice.service;

import com.promptcourse.courseservice.dto.SectionForProgressDto;
import com.promptcourse.courseservice.model.Chapter;
import com.promptcourse.courseservice.model.Lecture;
import com.promptcourse.courseservice.model.Section;
import com.promptcourse.courseservice.model.Test;
import com.promptcourse.courseservice.repository.LectureRepository;
import com.promptcourse.courseservice.repository.SectionRepository;
import com.promptcourse.courseservice.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternalCourseService {

    private final SectionRepository sectionRepository;
    private final TestRepository testRepository;
    private  final LectureRepository lectureRepository;

    public SectionForProgressDto getSectionStructure(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found: " + sectionId));
        Long testId = testRepository.findBySectionId(sectionId).map(Test::getId).orElse(null);

        return SectionForProgressDto.builder()
                .sectionId(section.getId())
                .testId(testId)
                .chapters(section.getChapters().stream()
                        .sorted(Comparator.comparingInt(Chapter::getOrderIndex)) // Сортируем главы
                        .map(this::mapChapter)
                        .collect(Collectors.toList()))
                .build();
    }

    private SectionForProgressDto.ChapterForProgressDto mapChapter(Chapter chapter) {
        Long testId = testRepository.findByChapterId(chapter.getId()).map(Test::getId).orElse(null);
        return SectionForProgressDto.ChapterForProgressDto.builder()
                .chapterId(chapter.getId())
                .testId(testId)
                .lectures(chapter.getLectures().stream()
                        .sorted(Comparator.comparingInt(Lecture::getOrderIndex)) // Сортируем лекции
                        .map(this::mapLecture)
                        .collect(Collectors.toList()))
                .build();
    }

    private SectionForProgressDto.LectureForProgressDto mapLecture(Lecture lecture) {
        Long testId = testRepository.findByLectureId(lecture.getId()).map(Test::getId).orElse(null);
        return SectionForProgressDto.LectureForProgressDto.builder()
                .lectureId(lecture.getId())
                .testId(testId)
                .build();
    }
    public SectionForProgressDto.LectureForProgressDto getLectureInfo(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new RuntimeException("Lecture not found"));
        return mapLecture(lecture); // Используем наш существующий маппер
    }
}