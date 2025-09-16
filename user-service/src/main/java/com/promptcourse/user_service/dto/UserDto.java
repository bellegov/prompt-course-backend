package com.promptcourse.user_service.dto;

import com.promptcourse.user_service.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private Long telegramId;
    private String email;
    private String nickname;
    private Role role;
}
