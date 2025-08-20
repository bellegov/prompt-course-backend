package com.promptcourse.courseservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // --- НАШИ ЛОГИ ДЛЯ ОТЛАДКИ ---
        System.out.println("====== COURSE-SERVICE: JwtAuthFilter STARTED for path: " + request.getRequestURI() + " ======");
        // -----------------------------

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("====== COURSE-SERVICE: No JWT Token found, passing to next filter ======");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String subject = jwtService.extractSubject(jwt);

            System.out.println("====== COURSE-SERVICE: Token found. Subject: " + subject + " ======");

            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("====== COURSE-SERVICE: Security context is empty, trying to authenticate... ======");

                Claims claims = jwtService.extractClaim(jwt, Function.identity());
                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);
                Boolean isSubscribed = claims.get("isSubscribed", Boolean.class);
                if (isSubscribed == null) {
                    isSubscribed = false;
                }

                System.out.println("====== COURSE-SERVICE: Claims extracted. UserID: " + userId + ", Role: " + role + " ======");

                if (userId == null || role == null) {
                    System.out.println("====== COURSE-SERVICE: ERROR! UserID or Role is NULL in token! ======");
                    filterChain.doFilter(request, response);
                    return;
                }

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
                System.out.println("====== COURSE-SERVICE: Authorities created: " + authorities + " ======");

                UserPrincipal principal = new UserPrincipal(userId, subject, authorities, isSubscribed);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Устанавливаем аутентификацию в контекст
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("====== COURSE-SERVICE: Authentication successful! User is now authenticated. ======");
            }
        } catch (Exception e) {
            System.out.println("====== COURSE-SERVICE: ERROR during token processing: " + e.getMessage() + " ======");
        }

        filterChain.doFilter(request, response);
    }
}