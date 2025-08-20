package com.promptcourse.courseservice.controller;


import com.promptcourse.courseservice.dto.CourseOutlineDto;
import com.promptcourse.courseservice.dto.LectureContentDto;
import com.promptcourse.courseservice.dto.SubmitTestRequestDto;
import com.promptcourse.courseservice.dto.TestResultDto;
import com.promptcourse.courseservice.dto.UserTestViewDto;
import com.promptcourse.courseservice.security.UserPrincipal; // <-- Правильный импорт
import com.promptcourse.courseservice.service.CourseViewService;
import com.promptcourse.courseservice.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // <-- Импорт Spring Security
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class CourseController {

    private final CourseViewService courseViewService;
    private final TestService testService;

    // === ПОЛУЧЕНИЕ СТРУКТУРЫ КУРСА ===

    @GetMapping("/outline")
    public ResponseEntity<CourseOutlineDto> getCourseOutline(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getId();
        boolean isSubscribed = principal.isSubscribed();// Временная логика

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
    public ResponseEntity<TestResultDto> submitTest(
            @PathVariable Long testId,
            @RequestBody SubmitTestRequestDto submission,
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getId();
        boolean isSubscribed = principal.isSubscribed();

        return ResponseEntity.ok(testService.submitTest(testId, userId, isSubscribed, submission));
    }
}