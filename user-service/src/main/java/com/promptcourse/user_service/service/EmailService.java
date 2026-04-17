package com.promptcourse.user_service.service;

import com.promptcourse.user_service.dto.BePaidNotification;
import com.promptcourse.user_service.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final RestTemplate restTemplate;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${app.mail.from}")
    private String mailFrom;

    // 1. Отправка квитанции об оплате
    @Async
    public void sendReceipt(User user, BePaidNotification.Transaction payment) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Cannot send receipt to userId: {}. Email is missing.", user.getId());
            return;
        }

        String subject = "Квитанция об оплате подписки - Promtly";
        String text = String.format(
                "Здравствуйте, %s!\n\nВы успешно оплатили подписку на 1 месяц.\nСумма: %.2f %s\nID транзакции: %s\n\nСпасибо, что выбрали нас!",
                user.getNickname(),
                payment.getAmount().getTotal() / 100.0,
                payment.getAmount().getCurrency(),
                payment.getUid()
        );

        sendEmailViaResend(user.getEmail(), subject, text);
    }

    // 2. Отправка письма для сброса пароля
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        String subject = "Сброс пароля для вашего аккаунта - Promtly";
        String text = "Здравствуйте!\n\nВы запросили сброс пароля. " +
                "Для установки нового пароля, пожалуйста, перейдите по ссылке:\n\n" +
                resetUrl + "\n\nЕсли вы не запрашивали сброс, просто проигнорируйте это письмо.";

        sendEmailViaResend(toEmail, subject, text);
    }

    // Внутренний метод для HTTP-запроса к API Resend
    private void sendEmailViaResend(String toEmail, String subject, String text) {
        try {
            String url = "https://api.resend.com/emails";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendApiKey);

            // Формируем JSON-тело согласно документации Resend
            Map<String, Object> body = Map.of(
                    "from", mailFrom,
                    "to", List.of(toEmail),
                    "subject", subject,
                    "text", text
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // Отправляем POST запрос
            restTemplate.postForEntity(url, request, String.class);
            log.info("Email successfully sent to {} via Resend API", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email to {} via Resend API", toEmail, e);
        }
    }
}