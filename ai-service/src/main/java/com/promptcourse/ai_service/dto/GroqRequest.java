package com.promptcourse.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GroqRequest {
    private String model;
    private List<GroqMessage> messages;
}