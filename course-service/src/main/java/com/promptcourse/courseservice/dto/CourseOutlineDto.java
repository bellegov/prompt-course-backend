package com.promptcourse.courseservice.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
@Builder
public class CourseOutlineDto implements Serializable {
    private List<SectionOutlineDto> sections;
    private int livesLeft;
    private String recoveryTimeLeft;
    private int totalCourseProgress;

    @Data
    @Builder
    public static class SectionOutlineDto implements Serializable {
        private Long id;
        private String title;
        private String description;
        private List<ChapterOutlineDto> chapters;
        private Long testId; // <-- ДОБАВЛЕНО: ID теста для этого раздела (или null)
        private int progressPercentage;
        private boolean isUnlocked;
        private Integer iconId;
        private boolean isPremium;
    }

    @Data
    @Builder
    public static class ChapterOutlineDto implements Serializable {
        private Long id;
        private String title;
        private Long testId;
        private List<LectureOutlineDto> lectures;
        private boolean isUnlocked; // <-- НОВОЕ ПОЛЕ: для отрисовки главой
    }

    @Data
    @Builder
    public static class LectureOutlineDto implements Serializable {
        private Long id;
        private String title;
        private Long testId;
        private LectureState state; // <-- НОВОЕ ПОЛЕ: LOCKED, UNLOCKED или COMPLETED
        private boolean promptsAvailable; // <-- НОВОЕ ПОЛЕ: для кнопки "Запросы"
    }
}
