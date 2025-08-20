package com.promptcourse.userservice.dto;

import lombok.*;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
}
