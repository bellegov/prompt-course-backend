package com.promptcourse.progress_service.controller;

import com.promptcourse.progress_service.model.UserGlobalProgress;
import com.promptcourse.progress_service.service.GlobalProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GlobalProgressController {

    private final GlobalProgressService globalProgressService;

    // Публичный эндпоинт для завершения раздела
    @PostMapping("/complete-section")
    public ResponseEntity<Void> completeSection(
            @RequestBody Map<String, Integer> payload,
            @RequestHeader("X-User-ID") Long userId // <-- Читаем ID из заголовка
    ) {
        Integer sectionOrderIndex = payload.get("sectionOrderIndex");
        if (sectionOrderIndex == null) {
            return ResponseEntity.badRequest().build();
        }
        globalProgressService.completeSection(userId, sectionOrderIndex);
        return ResponseEntity.ok().build();
    }

    // Внутренний эндпоинт для получения глобального прогресса
    @GetMapping("/internal/global-progress/{userId}")
    public ResponseEntity<UserGlobalProgress> getGlobalProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(globalProgressService.getGlobalProgress(userId));
    }
}