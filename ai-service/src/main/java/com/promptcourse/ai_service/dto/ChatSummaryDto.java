package com.promptcourse.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatSummaryDto {
    private Long id;
    private String title;
    private String model;
    private LocalDateTime createdAt;
}