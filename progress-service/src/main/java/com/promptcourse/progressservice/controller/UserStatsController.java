package com.promptcourse.progressservice.controller;

import com.promptcourse.progressservice.model.UserStats;
import com.promptcourse.progressservice.service.UserStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserStatsController {

    private final UserStatsService statsService;

    public UserStatsController(UserStatsService statsService) {
        this.statsService = statsService;
    }

    // Публичный
    @PostMapping("/stats/check-in")
    public ResponseEntity<Void> dailyCheckIn(@RequestHeader("X-User-ID") Long userId) { // <-- Читаем ID из заголовка
        statsService.processDailyLogin(userId);
        return ResponseEntity.ok().build();
    }

    // Внутренний
    @PostMapping("/internal/increment-lectures")
    public ResponseEntity<Void> incrementLectures(@RequestParam Long userId) {
        statsService.incrementLecturesCompleted(userId);
        return ResponseEntity.ok().build();
    }

    // Внутренний
    @GetMapping("/internal/stats/users/{userId}")
    public ResponseEntity<UserStats> getStatsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(statsService.getStatsForUser(userId));
    }
}