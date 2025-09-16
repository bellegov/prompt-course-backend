package com.promptcourse.user_service.dto;

import lombok.*;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelegramAuthRequest {
    private String initData; }