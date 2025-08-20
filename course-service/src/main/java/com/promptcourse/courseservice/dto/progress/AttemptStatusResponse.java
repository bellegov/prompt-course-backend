package com.promptcourse.courseservice.dto.progress;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttemptStatusResponse {
    private boolean canAttempt; // Можно ли вообще пытаться
    private String message;
    private int livesLeft;      // Сколько жизней осталось
}
