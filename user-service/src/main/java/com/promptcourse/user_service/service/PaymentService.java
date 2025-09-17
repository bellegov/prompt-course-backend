package com.promptcourse.user_service.service;
import com.promptcourse.user_service.client.CourseServiceClient;
import com.promptcourse.user_service.dto.BePaidNotification;
import com.promptcourse.user_service.dto.CreatePaymentResponse;
import com.promptcourse.user_service.model.Subscription;
import com.promptcourse.user_service.model.SubscriptionStatus;
import com.promptcourse.user_service.model.User;
import com.promptcourse.user_service.repository.SubscriptionRepository;
import com.promptcourse.user_service.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor // Используем Lombok для конструктора
public class PaymentService {

    // --- ЗАВИСИМОСТИ ---
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;
    private final CourseServiceClient courseServiceClient;
    private final RestTemplate restTemplate; // Spring создаст этот бин сам
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    // --- НАСТРОЙКИ ИЗ application.properties ---
    @Value("${bepaid.shop-id}")
    private String shopId;
    @Value("${bepaid.secret-key}")
    private String secretKey;
    @Value("${app.frontend.url}")
    private String frontendUrl;
    @Value("${app.webhook.url}") // Новый URL для вебхука
    private String webhookUrl;


    /**
     * Создает платеж через API bePaid и возвращает ссылку для оплаты.
     */
    public CreatePaymentResponse createPayment(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for payment creation: " + userId));

        // Убедимся, что у пользователя есть email, перед созданием платежа
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalStateException("Cannot create payment for user without an email.");
        }

        String url = "https://checkout.bepaid.by/ctp/api/checkouts";

        HttpHeaders headers = new HttpHeaders();
        // Устанавливаем Basic Auth с помощью ключей. RestTemplate сам их закодирует.
        headers.setBasicAuth(shopId, secretKey, StandardCharsets.UTF_8);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "checkout", Map.of(
                        "version", 2.1,
                        "test", true, // Для разработки. В продакшене поменять на false.
                        "transaction_type", "payment",
                        "order", Map.of(
                                "currency", "USD",
                                "amount", 500, // $5.00 = 500 центов
                                "description", "Подписка PRO на 1 месяц"
                        ),
                        "settings", Map.of(
                                "success_url", frontendUrl + "/payment-success",
                                "decline_url", frontendUrl + "/payment-failed",
                                "fail_url", frontendUrl + "/payment-failed",
                                "notification_url", webhookUrl, // Наш URL для веб-хука
                                "customer_id", userId.toString()
                        )
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> checkout = (Map<String, Object>) response.getBody().get("checkout");
        String checkoutUrl = (String) checkout.get("redirect_url");

        return CreatePaymentResponse.builder()
                .confirmationUrl(checkoutUrl)
                .build();
    }

    /**
     * Обрабатывает уведомление (веб-хук) от bePaid.
     */
    @Transactional
    public void processNotification(BePaidNotification notification) {
        BePaidNotification.Transaction tx = notification.getTransaction();
        if (!"successful".equals(tx.getStatus())) {
            log.info("Ignored bePaid notification with status: {}", tx.getStatus());
            return;
        }

        Long userId = Long.parseLong(tx.getCustomerId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found from notification: " + userId));

        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElse(Subscription.builder().user(user).build());

        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);

        log.info("Subscription activated for userId: {}", userId);

        // Сбрасываем кэш для пользователя
        try {
            courseServiceClient.clearOutlineCache(userId);
            log.info("Cache invalidation requested for userId {} after subscription activation.", userId);
        } catch (Exception e) {
            log.error("Failed to request cache invalidation for userId {} after subscription activation.", userId, e);
        }

        // Асинхронно отправляем квитанцию
        emailService.sendReceipt(user, tx);
    }
}