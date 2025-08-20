package com.promptcourse.courseservice.config;

import com.promptcourse.courseservice.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // Оставляем ее, она не мешает
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // --- НАШИ НОВЫЕ, ЯВНЫЕ ПРАВИЛА ---
                        // Разрешаем доступ к админским путям только с ролью ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Разрешаем доступ к пользовательским путям с ролью USER
                        .requestMatchers("/user/**").hasRole("USER")
                        // Внутренние пути для общения сервисов доступны всем внутри сети
                        .requestMatchers("/internal/**").permitAll()
                        // Все остальные запросы (если такие появятся) должны быть аутентифицированы
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}