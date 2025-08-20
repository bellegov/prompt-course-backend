package com.promptcourse.userservice.dto;
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

    // Данные из progress-service
    private int totalLecturesCompleted;
    private int consecutiveDays;
    private int totalActiveDays;
}
