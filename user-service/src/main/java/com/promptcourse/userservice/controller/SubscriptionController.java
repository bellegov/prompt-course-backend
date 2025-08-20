package com.promptcourse.userservice.controller;
import com.promptcourse.userservice.dto.CreatePaymentResponse;
import com.promptcourse.userservice.security.UserPrincipal;
import com.promptcourse.userservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- Импорт
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/subscriptions") // <-- ПРАВИЛЬНЫЙ ПУТЬ
@RequiredArgsConstructor
public class SubscriptionController {

    private final PaymentService paymentService;

    // Этот эндпоинт теперь будет доступен по POST /api/users/users/subscriptions
    @PostMapping
    @PreAuthorize("hasRole('USER')") // Дополнительно защищаем, разрешая только USER
    public ResponseEntity<CreatePaymentResponse> createPayment(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(paymentService.createPayment(principal.getId()));
    }
}
