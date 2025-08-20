package com.promptcourse.userservice.service;

import com.promptcourse.userservice.dto.UpdatePasswordRequest;
import com.promptcourse.userservice.dto.UpdateUserRequest;
import com.promptcourse.userservice.dto.UserDto;
import com.promptcourse.userservice.model.Role;
import com.promptcourse.userservice.model.User;
import com.promptcourse.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto findUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::mapToUserDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @Transactional
    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // ---ПРОВЕРКА ---
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {

            // ЭТОТ КОД ВНУТРИ IF ВЫПОЛНЯЕТСЯ, ТОЛЬКО ЕСЛИ УСЛОВИЕ ВЫШЕ ВЕРНО:
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalStateException("Email " + request.getEmail() + " is already taken.");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }

        User updatedUser = userRepository.save(user);
        return mapToUserDto(updatedUser);
    }

    public UserDto updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Преобразуем строку в Enum, чтобы избежать ошибок
        Role newRole = Role.valueOf(roleName.toUpperCase());
        user.setRole(newRole);

        User updatedUser = userRepository.save(user);
        return mapToUserDto(updatedUser);
    }
    public void updateUserPassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Хэшируем новый пароль перед сохранением
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    // Вспомогательный метод, чтобы не отдавать наружу пароль
    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .telegramId(user.getTelegramId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }
}
