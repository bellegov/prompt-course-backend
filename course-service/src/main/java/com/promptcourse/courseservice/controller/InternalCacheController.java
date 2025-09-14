package com.promptcourse.courseservice.controller;

import com.promptcourse.courseservice.service.CourseViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/cache") // Внутренний путь, недоступный снаружи через шлюз
@RequiredArgsConstructor
public class InternalCacheController {

    private final CourseViewService courseViewService;

    @PostMapping("/clear-outline")
    public ResponseEntity<Void> clearOutlineCache(@RequestParam Long userId) {
        courseViewService.clearOutlineCacheForUser(userId);
        return ResponseEntity.ok().build();
    }
}