package com.promptcourse.progressservice.controller;
import com.promptcourse.progressservice.model.UserStats;
import com.promptcourse.progressservice.security.UserPrincipal;
import com.promptcourse.progressservice.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
public class UserStatsController {

    private final UserStatsService statsService;

    public UserStatsController(UserStatsService statsService) {
        this.statsService = statsService;
    }

    // Публичный эндпоинт
    @PostMapping("/stats/check-in")
    public ResponseEntity<Void> dailyCheckIn(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        statsService.processDailyLogin(principal.getId());
        return ResponseEntity.ok().build();
    }

    // Внутренний эндпоинт
    @PostMapping("/internal/increment-lectures")
    public ResponseEntity<Void> incrementLectures(@RequestParam Long userId) {
        statsService.incrementLecturesCompleted(userId);
        return ResponseEntity.ok().build();
    }

    // Внутренний эндпоинт для профиля
    @GetMapping("/internal/stats/users/{userId}")
    public ResponseEntity<UserStats> getStatsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(statsService.getStatsForUser(userId));
    }
}
