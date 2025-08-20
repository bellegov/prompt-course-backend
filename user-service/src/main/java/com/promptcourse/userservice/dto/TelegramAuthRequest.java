package com.promptcourse.userservice.dto;

import lombok.*;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TelegramAuthRequest {
    private String initData; }