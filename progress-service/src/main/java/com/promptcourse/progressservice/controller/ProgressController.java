package com.promptcourse.progressservice.controller;

import com.promptcourse.progressservice.dto.MarkCompletedRequest;
import com.promptcourse.progressservice.dto.ProgressRequest;
import com.promptcourse.progressservice.dto.UserProgressResponse;
import com.promptcourse.progressservice.security.UserPrincipal; // <-- Импорт
import com.promptcourse.progressservice.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // <-- Импорт
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // Внутренний эндпоинт, остается без изменений
    @PostMapping("/internal/get-progress")
    public ResponseEntity<UserProgressResponse> getProgressForUser(@RequestBody ProgressRequest request) {

        return ResponseEntity.ok(progressService.getUserProgress(request.getUserId(), request.getSectionId(), request.isSubscribed()));
    }

    // Публичный эндпоинт, ТЕПЕРЬ БЕЗОПАСНЫЙ
    @PostMapping("/complete-lecture")
    public ResponseEntity<Void> markLectureCompleted(
            @RequestBody MarkCompletedRequest request,
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getId();

        // Передаем в сервис ID из токена
        progressService.markLectureAsCompleted(userId, request);
        return ResponseEntity.ok().build();
    }

    // Внутренний эндпоинт для получения ID пройденных лекций
    @GetMapping("/internal/users/{userId}/completed-lectures")
    public ResponseEntity<Set<Long>> getCompletedLectures(@PathVariable Long userId) {
        return ResponseEntity.ok(progressService.getCompletedLectureIds(userId));
    }
}