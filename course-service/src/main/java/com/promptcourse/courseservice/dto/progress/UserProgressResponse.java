package com.promptcourse.courseservice.dto.progress;


import com.promptcourse.courseservice.dto.LectureState;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class UserProgressResponse {
    // Карта: ID лекции -> ее статус (LOCKED, UNLOCKED, COMPLETED)
    private Map<Long, LectureState> lectureStates;
    // Карта: ID главы -> разблокирована ли она
    private Map<Long, Boolean> chapterStates;
    // Процент для прогресс-бара
    private int progressPercentage;
    private int livesLeft;
    private String recoveryTimeLeft;
}
