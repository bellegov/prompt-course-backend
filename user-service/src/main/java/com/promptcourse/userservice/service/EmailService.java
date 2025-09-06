package com.promptcourse.userservice.service;
import com.promptcourse.userservice.dto.YooMoneyNotification;
import com.promptcourse.userservice.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Async // <-- Аннотация здесь, в отдельном сервисе
    public void sendReceipt(User user, YooMoneyNotification.PaymentObject payment) {
        if (user.getEmail() == null) {
            log.warn("Cannot send receipt to userId: {}. Email is missing.", user.getId());
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(user.getEmail());
            message.setSubject("Квитанция об оплате подписки");
            message.setText(String.format(
                    "Здравствуйте, %s!\n\nВы успешно оплатили подписку на 1 месяц.\nСумма: %s %s\nID транзакции: %s\n\nСпасибо, что выбрали нас!",
                    user.getNickname(),
                    payment.getAmount().getValue(),
                    payment.getAmount().getCurrency(),
                    payment.getId()
            ));
            mailSender.send(message);
            log.info("Receipt email sent successfully to {}", user.getEmail());
        } catch (MailException e) {
            log.error("Failed to send receipt email for user {}", user.getEmail(), e);
        }
    }
}
