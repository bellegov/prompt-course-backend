package com.promptcourse.userservice.service;
import com.promptcourse.userservice.model.PasswordResetToken;
import com.promptcourse.userservice.model.User;
import com.promptcourse.userservice.repository.PasswordResetTokenRepository;
import com.promptcourse.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    @Value("${app.mail.from}")
    private String mailFrom;

    // URL вашего фронтенда, куда будет вести ссылка из письма
    @Value("${app.frontend.url}")
    private String frontendUrl;

    // Метод для создания и отправки токена
    public void createAndSendPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null); // Не выбрасываем ошибку, чтобы не раскрывать, существует ли email

        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return; // Просто ничего не делаем
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);
        sendResetEmail(user, token);
    }

    // Метод для сброса пароля
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Password reset token has expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Удаляем токен после использования, чтобы он был одноразовым
        tokenRepository.delete(resetToken);
    }

    private void sendResetEmail(User user, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("Сброс пароля для вашего аккаунта");
        message.setText("Здравствуйте!\n\nВы запросили сброс пароля. " +
                "Для установки нового пароля, пожалуйста, перейдите по ссылке:\n" +
                resetUrl + "\n\nЕсли вы не запрашивали сброс, просто проигнорируйте это письмо.");

        mailSender.send(message);
    }
}
