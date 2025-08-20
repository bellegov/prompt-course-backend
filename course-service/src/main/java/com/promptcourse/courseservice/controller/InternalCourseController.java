package com.promptcourse.courseservice.controller;

import com.promptcourse.courseservice.dto.SectionForProgressDto;
import com.promptcourse.courseservice.service.InternalCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/courses") // Внутренний путь, который не будет в шлюзе
@RequiredArgsConstructor
public class InternalCourseController {

    private final InternalCourseService internalCourseService;

    @GetMapping("/sections/{sectionId}/structure")
    public ResponseEntity<SectionForProgressDto> getSectionStructureForProgress(@PathVariable Long sectionId) {
        return ResponseEntity.ok(internalCourseService.getSectionStructure(sectionId));
    }
    @GetMapping("/lectures/{lectureId}/info")
    public ResponseEntity<SectionForProgressDto.LectureForProgressDto> getLectureInfo(@PathVariable Long lectureId) {
        return ResponseEntity.ok(internalCourseService.getLectureInfo(lectureId));
    }
}
