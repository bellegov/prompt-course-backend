package com.promptcourse.userservice.controller;
import com.promptcourse.userservice.dto.*;
import com.promptcourse.userservice.service.AuthService;
import com.promptcourse.userservice.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/telegram")
    public ResponseEntity<AuthResponse> authWithTelegram(@RequestBody TelegramAuthRequest request) {
        return ResponseEntity.ok(service.authWithTelegram(request.getInitData()));
    }
    // 1. Запрос на сброс пароля
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        passwordResetService.createAndSendPasswordResetToken(email);
        // Всегда возвращаем 200 OK, чтобы не раскрывать, существует ли email в базе
        return ResponseEntity.ok().build();
    }

    // 2. Установка нового пароля
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam("token") String token, @RequestBody Map<String, String> payload) {
        String newPassword = payload.get("newPassword");
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok().build();
    }
}
