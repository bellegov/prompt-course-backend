package com.promptcourse.progressservice.controller;
import com.promptcourse.progressservice.dto.AttemptRequest;
import com.promptcourse.progressservice.dto.AttemptStatusResponse;
import com.promptcourse.progressservice.service.AttemptService;
import com.promptcourse.progressservice.service.CompletedTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/attempts")
@RequiredArgsConstructor
public class InternalAttemptController {

    private final AttemptService attemptService;
    private final CompletedTestService completedTestService;

    // Этот эндпоинт остается без изменений, так как его вызывает course-service,
    // который сам получает userId из заголовка и передает его сюда.
    @GetMapping("/status")
    public ResponseEntity<AttemptStatusResponse> getAttemptStatus(@RequestParam Long userId, @RequestParam boolean isSubscribed) {
        return ResponseEntity.ok(attemptService.checkCanAttempt(userId, isSubscribed));
    }

    @PostMapping("/record-failure")
    public ResponseEntity<Void> recordFailure(@RequestParam Long userId, @RequestParam boolean isSubscribed) {
        attemptService.recordFailedAttempt(userId, isSubscribed);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/record-success")
    public ResponseEntity<Void> recordSuccess(@RequestBody AttemptRequest request) {
        completedTestService.markTestAsCompleted(request.getUserId(), request.getTestId());
        return ResponseEntity.ok().build();
    }
}