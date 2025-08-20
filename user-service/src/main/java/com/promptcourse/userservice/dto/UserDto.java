package com.promptcourse.userservice.dto;

import com.promptcourse.userservice.model.Role;
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
