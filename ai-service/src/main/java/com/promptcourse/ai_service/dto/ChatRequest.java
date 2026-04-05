package com.promptcourse.ai_service.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String title;
    private String model;
}