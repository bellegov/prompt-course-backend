package com.promptcourse.userservice.controller;

import com.promptcourse.userservice.dto.UpdatePasswordRequest;
import com.promptcourse.userservice.dto.UpdateUserRequest;
import com.promptcourse.userservice.dto.UserDto;
import com.promptcourse.userservice.model.User;
import com.promptcourse.userservice.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users") // Общий путь для всех админских операций с пользователями
@RequiredArgsConstructor
// ВАЖНО: Эта аннотация разрешает доступ к контроллеру только пользователям с ролью ADMIN
// Чтобы она заработала, нужно будет сделать небольшую донастройку.
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminUserService adminUserService;

    // READ (Получить всех пользователей)
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.findAllUsers());
    }

    // READ (Получить одного пользователя по ID)
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.findUserById(userId));
    }
    // UPDATE (Обновить данные пользователя: никнейм, email)
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(userId, request));
    }

    // UPDATE (Изменить роль пользователя)
    @PutMapping("/{userId}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long userId, @RequestParam String role) {
        return ResponseEntity.ok(adminUserService.updateUserRole(userId, role));
    }
    // UPDATE (Изменить пароль пользователя)
    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updateUserPassword(@PathVariable Long userId, @RequestBody UpdatePasswordRequest request) {
        adminUserService.updateUserPassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    // DELETE (Удалить пользователя)
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
