package com.promptcourse.userservice.dto.progress;
import lombok.Data;
import java.time.LocalDate;

// Это DTO, а не сущность
@Data
public class UserStats {
    private Long id;
    private Long userId;
    private int totalLecturesCompleted;
    private int consecutiveDays;
    private int totalActiveDays;
    private LocalDate lastLoginDate;
}