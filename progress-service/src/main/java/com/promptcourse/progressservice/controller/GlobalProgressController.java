package com.promptcourse.progressservice.controller;

import com.promptcourse.progressservice.model.UserGlobalProgress;
import com.promptcourse.progressservice.security.UserPrincipal;
import com.promptcourse.progressservice.service.GlobalProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Integer sectionOrderIndex = payload.get("sectionOrderIndex");
        if (sectionOrderIndex == null) {
            return ResponseEntity.badRequest().build();
        }
        globalProgressService.completeSection(principal.getId(), sectionOrderIndex);
        return ResponseEntity.ok().build();
    }

    // Внутренний эндпоинт для получения глобального прогресса
    @GetMapping("/internal/global-progress/{userId}")
    public ResponseEntity<UserGlobalProgress> getGlobalProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(globalProgressService.getGlobalProgress(userId));
    }
}
