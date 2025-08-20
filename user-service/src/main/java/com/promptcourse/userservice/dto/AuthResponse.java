package com.promptcourse.userservice.dto;

import lombok.*;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
}
