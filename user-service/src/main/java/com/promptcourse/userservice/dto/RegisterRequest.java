package com.promptcourse.userservice.dto;

import lombok.*;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String nickname;
    private String email;
    private String password;
}