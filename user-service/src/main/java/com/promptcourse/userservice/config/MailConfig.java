package com.promptcourse.userservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    // Этот бин будет создаваться ТОЛЬКО когда мы НЕ в продакшене.
    @Bean
    @Profile("!prod") // Активируется для всех профилей, кроме "prod"
    public JavaMailSender mockMailSender() {
        log.warn("!!! ATTENTION: Using MockMailSender. Emails will be printed to console only. !!!");

        // Мы возвращаем "фальшивого" почтальона, который переопределяет метод send
        return new JavaMailSenderImpl() {
            @Override
            public void send(SimpleMailMessage simpleMessage) {
                // Вместо отправки, просто выводим письмо в лог
                log.info("--- MOCK EMAIL START ---");
                log.info("To: {}", (Object) simpleMessage.getTo());
                log.info("From: {}", simpleMessage.getFrom());
                log.info("Subject: {}", simpleMessage.getSubject());
                log.info("Body: {}", simpleMessage.getText());
                log.info("--- MOCK EMAIL END ---");
            }
        };
    }
}