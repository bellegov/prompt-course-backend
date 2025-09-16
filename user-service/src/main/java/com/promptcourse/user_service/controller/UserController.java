package com.promptcourse.user_service.controller;
import com.promptcourse.user_service.dto.*;
import com.promptcourse.user_service.dto.course.UserPromptsDto;
import com.promptcourse.user_service.security.UserPrincipal;
import com.promptcourse.user_service.service.PaymentService;
import com.promptcourse.user_service.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final PaymentService paymentService;

    // === ПРОФИЛЬ ===

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userProfileService.getUserProfile(principal.getId()));
    }

    @PutMapping("/profile/avatar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> updateAvatar(
            @RequestBody Map<String, Integer> payload,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userProfileService.updateUserAvatar(principal.getId(), payload.get("avatarId"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile/prompts")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserPromptsDto>> getPrompts(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userProfileService.getUserPrompts(principal.getId()));
    }


    // ------------------------------------

    // === СВЯЗКА И ОБЪЕДИНЕНИЕ АККАУНТОВ ===
    @PostMapping("/profile/link-account")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> linkAccount(
            @RequestBody LinkAccountRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userProfileService.linkAccount(principal.getId(), request);
        return ResponseEntity.ok().build();
    }

    // === ПОДПИСКИ ===
    @PostMapping("/subscriptions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CreatePaymentResponse> createPayment(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(paymentService.createPayment(principal.getId()));
    }
}