package com.promptcourse.courseservice.client;
import com.promptcourse.courseservice.dto.progress.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PROGRESS-SERVICE")
public interface ProgressServiceClient {

    @PostMapping("/internal/get-progress")
    UserProgressResponse getProgressForUser(@RequestBody ProgressRequest request);

    @GetMapping("/internal/attempts/status")
    AttemptStatusResponse getAttemptStatus(@RequestParam("userId") Long userId, @RequestParam("isSubscribed") boolean isSubscribed);

    @PostMapping("/internal/attempts/record-failure")
    void recordFailure(@RequestParam("userId") Long userId, @RequestParam("isSubscribed") boolean isSubscribed);

    // --- ВОССТАНОВЛЕННЫЙ МЕТОД ---
    @PostMapping("/internal/attempts/record-success")
    void recordSuccess(@RequestBody AttemptRequest request);

    // --- НОВЫЙ МЕТОД ---
    @GetMapping("/internal/global-progress/{userId}")
    UserGlobalProgress getGlobalProgress(@PathVariable("userId") Long userId);
}