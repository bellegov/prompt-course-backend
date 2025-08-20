package com.promptcourse.userservice.controller;


import com.promptcourse.userservice.dto.*;
import com.promptcourse.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

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
}
