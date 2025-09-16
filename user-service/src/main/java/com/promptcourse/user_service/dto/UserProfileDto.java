package com.promptcourse.user_service.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDto {
    // Данные из user-service
    private Long id;
    private String nickname;
    private String email;
    private Integer avatarId;
    private boolean isSubscribed;
    private Long telegramId;

    // Данные из progress-service
    private int totalLecturesCompleted;
    private int consecutiveDays;
    private int totalActiveDays;
}
