package com.promptcourse.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptcourse.userservice.dto.*;
import com.promptcourse.userservice.model.Role;
import com.promptcourse.userservice.model.User;
import com.promptcourse.userservice.repository.UserRepository;
import com.promptcourse.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${telegram.bot.token}")
    private String botToken;

    public AuthResponse register(RegisterRequest request) {
        var user = User.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = repository.findByEmail(request.getEmail()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse authWithTelegram(String initData) {
        /*
        if (!isInitDataValid(initData)) {
            throw new IllegalArgumentException("Invalid initData: hash validation failed");
        }

         */
        try {
            Map<String, String> params = parseInitData(initData);
            String userJsonString = params.get("user");
            if (userJsonString == null) throw new IllegalArgumentException("User data not found in initData");

            Map<String, Object> userMap = objectMapper.readValue(userJsonString, Map.class);
            Long telegramId = Long.parseLong(userMap.get("id").toString());
            String nickname = userMap.get("first_name").toString();

            User user = repository.findByTelegramId(telegramId).orElseGet(() -> repository.save(
                    User.builder().telegramId(telegramId).nickname(nickname).role(Role.USER).build()
            ));

            var jwtToken = jwtService.generateToken(user);
            return AuthResponse.builder().token(jwtToken).build();
        } catch (Exception e) {
            throw new RuntimeException("Error processing Telegram initData", e);
        }
    }

    private boolean isInitDataValid(String initData) {
        Map<String, String> params = parseInitData(initData);
        String receivedHash = params.remove("hash");
        if (receivedHash == null) return false;

        String dataCheckString = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .sorted().collect(Collectors.joining("\n"));

        byte[] secretKey = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, "WebAppData").hmac(botToken);
        String calculatedHash = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secretKey).hmacHex(dataCheckString);

        return calculatedHash.equals(receivedHash);
    }

    private Map<String, String> parseInitData(String initData) {
        return Arrays.stream(initData.split("&"))
                .map(pair -> {
                    int idx = pair.indexOf("=");
                    return new String[]{
                            URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                            URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
                    };
                }).collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
    }
}
