package com.promptcourse.userservice.dto;
import lombok.Data;
@Data
public class UpdateUserRequest {
    private String nickname;
    private String email;
    // Мы не включаем сюда роль, так как для нее есть отдельный, более безопасный эндпоинт.
}
