package com.promptcourse.courseservice.controller;


import com.promptcourse.courseservice.dto.CourseOutlineDto;
import com.promptcourse.courseservice.dto.LectureContentDto;
import com.promptcourse.courseservice.dto.SubmitTestRequestDto;
import com.promptcourse.courseservice.dto.TestResultDto;
import com.promptcourse.courseservice.dto.UserTestViewDto;

import com.promptcourse.courseservice.service.CourseViewService;
import com.promptcourse.courseservice.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class CourseController {
    private final CourseViewService courseViewService;
    private final TestService testService;

    @GetMapping("/outline")
    public ResponseEntity<CourseOutlineDto> getCourseOutline(@RequestHeader("X-User-ID") Long userId, @RequestHeader("X-User-Subscribed") Boolean isSubscribed) {
        return ResponseEntity.ok(courseViewService.getCourseOutline(userId, isSubscribed));
    }

    @GetMapping("/lectures/{lectureId}")
    public ResponseEntity<LectureContentDto> getLectureContent(@PathVariable Long lectureId) {
        return ResponseEntity.ok(courseViewService.getLectureContent(lectureId));
    }

    // === ПРОХОЖДЕНИЕ ТЕСТОВ ===

    @GetMapping("/tests/{testId}")
    public ResponseEntity<UserTestViewDto> getTest(@PathVariable Long testId) {
        return ResponseEntity.ok(testService.getTestForUser(testId));
    }

    // --- НАШ ОБНОВЛЕННЫЙ МЕТОД ---
    @PostMapping("/tests/{testId}/submit")
    public ResponseEntity<TestResultDto> submitTest(@PathVariable Long testId, @RequestBody SubmitTestRequestDto submission, @RequestHeader("X-User-ID") Long userId, @RequestHeader("X-User-Subscribed") Boolean isSubscribed) {
        return ResponseEntity.ok(testService.submitTest(testId, userId, isSubscribed, submission));
    }
}