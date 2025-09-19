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

    @PostMapping("/complete-section")
    public ResponseEntity<Void> completeSection(
            @RequestBody Map<String, Object> payload, // <-- меняем тип, чтобы принять и строку, и число
            @RequestHeader("X-User-ID") Long userId
    ) {
        Integer sectionOrderIndex = (Integer) payload.get("sectionOrderIndex");
        Integer sectionIdInt = (Integer) payload.get("sectionId"); // JSON передаст как Integer

        if (sectionOrderIndex == null || sectionIdInt == null) {
            return ResponseEntity.badRequest().build();
        }
        Long sectionId = Long.valueOf(sectionIdInt);

        globalProgressService.completeSection(userId, sectionOrderIndex, sectionId);
        return ResponseEntity.ok().build();
    }

    // Внутренний эндпоинт для получения глобального прогресса
    @GetMapping("/internal/global-progress/{userId}")
    public ResponseEntity<UserGlobalProgress> getGlobalProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(globalProgressService.getGlobalProgress(userId));
    }
}