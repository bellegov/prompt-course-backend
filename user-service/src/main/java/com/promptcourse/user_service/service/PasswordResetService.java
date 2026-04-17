package com.promptcourse.user_service.service;

import com.promptcourse.user_service.model.PasswordResetToken;
import com.promptcourse.user_service.model.User;
import com.promptcourse.user_service.repository.PasswordResetTokenRepository;
import com.promptcourse.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // Используем наш новый EmailService
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional
    public void createAndSendPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        // --- ИЩЕМ СТАРЫЙ ТОКЕН, ЧТОБЫ НЕ БЫЛО ОШИБКИ БАЗЫ ДАННЫХ ---
        Optional<PasswordResetToken> existingTokenOpt = tokenRepository.findByUser(user);
        PasswordResetToken resetToken;
        String token = UUID.randomUUID().toString();

        if (existingTokenOpt.isPresent()) {
            // Если пользователь уже запрашивал сброс, просто обновляем его старый токен
            resetToken = existingTokenOpt.get();
            resetToken.setToken(token);
            resetToken.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(15));
        } else {
            // Если запрашивает впервые - создаем новый
            resetToken = new PasswordResetToken(token, user);
        }

        tokenRepository.save(resetToken);

        // Отправляем письмо через Resend
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
    }

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

        // Удаляем использованный токен
        tokenRepository.delete(resetToken);
    }
}