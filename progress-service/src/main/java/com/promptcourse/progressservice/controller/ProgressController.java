package com.promptcourse.progressservice.controller;
import com.promptcourse.progressservice.dto.MarkCompletedRequest;
import com.promptcourse.progressservice.dto.ProgressRequest;
import com.promptcourse.progressservice.dto.UserProgressResponse;
import com.promptcourse.progressservice.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // Внутренний, без изменений
    @PostMapping("/internal/get-progress")
    public ResponseEntity<UserProgressResponse> getProgressForUser(@RequestBody ProgressRequest request) {
        return ResponseEntity.ok(progressService.getUserProgress(request.getUserId(), request.getSectionId(), request.isSubscribed()));
    }

    // Публичный
    @PostMapping("/complete-lecture")
    public ResponseEntity<Void> markLectureCompleted(
            @RequestBody MarkCompletedRequest request,
            @RequestHeader("X-User-ID") Long userId // <-- Читаем ID из заголовка
    ) {
        progressService.markLectureAsCompleted(userId, request);
        return ResponseEntity.ok().build();
    }

    // Внутренний
    @GetMapping("/internal/users/{userId}/completed-lectures")
    public ResponseEntity<Set<Long>> getCompletedLectures(@PathVariable Long userId) {
        return ResponseEntity.ok(progressService.getCompletedLectureIds(userId));
    }
}