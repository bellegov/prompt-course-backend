package com.promptcourse.userservice.controller;

import com.promptcourse.userservice.dto.SetPasswordRequest;
import com.promptcourse.userservice.dto.UserProfileDto;
import com.promptcourse.userservice.dto.course.UserPromptsDto;
import com.promptcourse.userservice.security.UserPrincipal;
import com.promptcourse.userservice.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- Импорт
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    // Явно говорим, что сюда можно пользователям с ролью USER или ADMIN
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userProfileService.getUserProfile(principal.getId()));
    }

    @PutMapping("/profile/avatar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> updateAvatar(
            @RequestBody Map<String, Integer> payload,
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Integer newAvatarId = payload.get("avatarId");
        if (newAvatarId == null) {
            return ResponseEntity.badRequest().build();
        }
        userProfileService.updateUserAvatar(principal.getId(), newAvatarId);
        return ResponseEntity.ok().build();
    }
    // Эндпоинт для установки/смены пароля
    @PutMapping("/profile/password")
    public ResponseEntity<Void> setPassword(
            @RequestBody SetPasswordRequest request,
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        userProfileService.setUserPassword(principal.getId(), request);
        return ResponseEntity.ok().build();
    }

    // Эндпоинт для установки email
    @PutMapping("/profile/email")
    public ResponseEntity<Void> setEmail(
            @RequestBody Map<String, String> payload,
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String email = payload.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        userProfileService.setUserEmail(principal.getId(), email);
        return ResponseEntity.ok().build();
    }
    // Эндпоинт для получения "библиотеки" промптов
    @GetMapping("/profile/prompts")
    public ResponseEntity<List<UserPromptsDto>> getPrompts(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userProfileService.getUserPrompts(principal.getId()));
    }
}