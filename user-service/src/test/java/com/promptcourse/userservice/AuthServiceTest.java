package com.promptcourse.userservice;

import com.promptcourse.userservice.dto.RegisterRequest;
import com.promptcourse.userservice.model.Role;
import com.promptcourse.userservice.model.User;
import com.promptcourse.userservice.repository.UserRepository;
import com.promptcourse.userservice.security.JwtService;
import com.promptcourse.userservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Эта аннотация "включает" магию Mockito в нашем тесте
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // @Mock: Создает "фальшивую" версию зависимости. Она не будет ходить в реальную БД.
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    // AuthenticationManager мы не мокаем, так как он не используется в методе register

    // @InjectMocks: Создает РЕАЛЬНЫЙ экземпляр AuthService,
    // но "внедряет" в него наши "фальшивые" зависимости (@Mock).
    @InjectMocks
    private AuthService authService;

    // Тестовый метод. Аннотация @Test говорит JUnit, что это нужно запустить.
    @Test
    void register_ShouldSaveUserWithHashedPasswordAndReturnToken() {
        // --- 1. ARRANGE (Подготовка) ---

        // Создаем входные данные для нашего метода
        RegisterRequest request = new RegisterRequest("TestUser", "test@test.com", "plainPassword123");

        // "Обучаем" наши заглушки: что они должны делать, когда их вызовут.

        // Когда будет вызван passwordEncoder.encode с ЛЮБОЙ строкой,
        // он должен вернуть эту строку с добавлением "_hashed".
        when(passwordEncoder.encode("plainPassword123")).thenReturn("plainPassword123_hashed");

        // Когда будет вызван userRepository.save с ЛЮБЫМ объектом User,
        // он должен просто вернуть этот же объект обратно (симулируя сохранение).
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Когда будет вызван jwtService.generateToken, он должен вернуть фейковый токен.
        when(jwtService.generateToken(any(User.class))).thenReturn("fake.jwt.token");


        // --- 2. ACT (Действие) ---

        // Вызываем реальный метод, который мы хотим протестировать
        authService.register(request);


        // --- 3. ASSERT (Проверка) ---

        // Создаем "перехватчик" (ArgumentCaptor), чтобы поймать реальный объект User,
        // который был передан в метод userRepository.save().
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        // Проверяем, что метод save у нашего фальшивого репозитория был вызван ровно 1 раз,
        // и "ловим" пользователя, которого пытались сохранить.
        verify(userRepository).save(userArgumentCaptor.capture());

        // Получаем "пойманного" пользователя
        User savedUser = userArgumentCaptor.getValue();

        // Проверяем, что все поля у сохраненного пользователя правильные
        assertNotNull(savedUser);
        assertEquals("TestUser", savedUser.getNickname());
        assertEquals("test@test.com", savedUser.getEmail());
        assertEquals(Role.USER, savedUser.getRole());

        // САМАЯ ГЛАВНАЯ ПРОВЕРКА: убеждаемся, что пароль, который мы пытались сохранить,
        // это именно та строка, которую вернул наш "фальшивый" passwordEncoder.
        // Это доказывает, что метод .encode() был вызван.
        assertEquals("plainPassword123_hashed", savedUser.getPassword());
    }
}
