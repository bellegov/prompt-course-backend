package com.promptcourse.userservice.service;
import com.promptcourse.userservice.client.CourseServiceClient;
import com.promptcourse.userservice.dto.CreatePaymentResponse;
import com.promptcourse.userservice.dto.YooMoneyNotification;
import com.promptcourse.userservice.model.Subscription;
import com.promptcourse.userservice.model.SubscriptionStatus;
import com.promptcourse.userservice.model.User;
import com.promptcourse.userservice.repository.SubscriptionRepository;
import com.promptcourse.userservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
   // private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;
    private final EmailService emailService;
    private final CourseServiceClient courseServiceClient;
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Value("${yoomoney.shop-id}")
    private String shopId;
    @Value("${yoomoney.secret-key}")
    private String secretKey;

    public PaymentService(UserRepository userRepository, SubscriptionRepository subscriptionRepository, EmailService emailService,CourseServiceClient courseServiceClient) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        //this.mailSender = mailSender;
        this.emailService = emailService;
        this.restTemplate = new RestTemplate();
        this.courseServiceClient = courseServiceClient;

    }
    // Метод, который будет вызван после того, как поля shopId и secretKey будут установлены
    @PostConstruct
    private void configureRestTemplate() {
        this.restTemplate.getInterceptors().add(
                new BasicAuthenticationInterceptor(shopId, secretKey, StandardCharsets.UTF_8)
        );
    }
    public CreatePaymentResponse createPayment(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // TODO: Проверить, нет ли у пользователя уже активной подписки

        String url = "https://api.yookassa.ru/v3/payments";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotence-Key", UUID.randomUUID().toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Формируем тело запроса для YooMoney
        Map<String, Object> body = Map.of(
                // --- ИЗМЕНЕНИЕ 1: ЦЕНА И ВАЛЮТА ---
                // YooKassa работает с рублями. Вам нужно будет указать эквивалент $5 в рублях.
                // Например, 450.00 рублей.
                "amount", Map.of("value", "450.00", "currency", "RUB"),
                "capture", true,
                // --- ИЗМЕНЕНИЕ 2: КУДА ВЕРНУТЬ ПОЛЬЗОВАТЕЛЯ ---
                // Здесь должна быть ссылка на страницу вашего приложения (сайта),
                // которая покажет "Оплата прошла успешно!".
                "confirmation", Map.of("type", "redirect", "return_url", "https://your-app.com/payment-success"),
                "description", "Подписка на Prompt Engineering Courses на 1 месяц",
                "metadata", Map.of("userId", userId.toString())
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map<String, Object> responseBody = response.getBody();
        String paymentId = (String) responseBody.get("id");
        Map<String, String> confirmation = (Map<String, String>) responseBody.get("confirmation");
        String confirmationUrl = confirmation.get("confirmation_url");

        return CreatePaymentResponse.builder()
                .paymentId(paymentId)
                .confirmationUrl(confirmationUrl)
                .build();
    }

    @Transactional
    public void processNotification(YooMoneyNotification notification) {
        if (!"notification".equals(notification.getType()) || !"payment.succeeded".equals(notification.getEvent())) {
            // Игнорируем все уведомления, кроме успешной оплаты
            return;
        }

        YooMoneyNotification.PaymentObject payment = notification.getObject();
        Long userId = Long.parseLong(payment.getMetadata().get("userId"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found from notification"));

        // Находим или создаем подписку
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElse(Subscription.builder().user(user).build());

        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusMonths(1)); // Подписка на месяц
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscriptionRepository.save(subscription);

        try {
            log.info("Subscription activated for userId: {}. Requesting cache invalidation.", userId);
            courseServiceClient.clearOutlineCache(userId);
        } catch (Exception e) {
            // Если course-service недоступен, это не должно ломать активацию подписки.
            // Просто логируем ошибку.
            log.error("Failed to request cache invalidation for userId {} after subscription activation.", userId, e);
        }

        // --- ИСПРАВЛЕНИЕ: Оборачиваем отправку письма в try-catch ---
        try {
            emailService.sendReceipt(user, payment);
        } catch (MailException e) {
            // Если отправка письма не удалась, мы не "роняем" всю транзакцию.
            // Мы просто логируем ошибку. Подписка при этом останется сохраненной.
            log.error("Failed to send receipt email for user {} after successful payment.", user.getEmail(), e);
        }
    }

}
