package com.promptcourse.user_service.controller;
import com.promptcourse.user_service.dto.BePaidNotification;
import com.promptcourse.user_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    // Публичный, незащищенный эндпоинт для bePaid
    @PostMapping("/payment/bepaid/webhook") // <-- Меняем URL
    public ResponseEntity<Void> bePaidWebhook(@RequestBody BePaidNotification notification) {
        log.info("Received bePaid notification: {}", notification);
        paymentService.processNotification(notification);
        return ResponseEntity.ok().build();
    }
}
