package com.promptcourse.progress_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor      // <-- ДОБАВЛЕНО
@AllArgsConstructor
public class SectionForProgressDto {
    private Long sectionId;
    private List<ChapterForProgressDto> chapters;

    @Data
    @Builder
    @NoArgsConstructor      // <-- ДОБАВЛЕНО
    @AllArgsConstructor
    public static class ChapterForProgressDto {
        private Long chapterId;
        private List<LectureForProgressDto> lectures;
    }

    @Data
    @Builder
    @NoArgsConstructor      // <-- ДОБАВЛЕНО
    @AllArgsConstructor
    public static class LectureForProgressDto {
        private Long lectureId;
        private Long testId;
    }
}
