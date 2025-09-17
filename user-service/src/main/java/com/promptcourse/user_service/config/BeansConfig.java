package com.promptcourse.user_service.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate; // <-- Импорт

@Configuration
public class BeansConfig {


    @Bean // <-- ДОБАВЬТЕ ЭТОТ БИН
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
