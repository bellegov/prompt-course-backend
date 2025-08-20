package com.promptcourse.progressservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class UserProgressResponse {
    private Map<Long, LectureState> lectureStates;
    private Map<Long, Boolean> chapterStates;
    private int progressPercentage;
    private int livesLeft;             // Текущее количество жизней
    private String recoveryTimeLeft; // Строка, показывающая, сколько осталось до восстановления (например, "2:59:59")
}
