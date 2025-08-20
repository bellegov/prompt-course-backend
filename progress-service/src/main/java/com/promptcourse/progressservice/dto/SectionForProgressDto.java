package com.promptcourse.progressservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SectionForProgressDto {
    private Long sectionId;
    private List<ChapterForProgressDto> chapters;

    @Data
    @Builder
    public static class ChapterForProgressDto {
        private Long chapterId;
        private List<LectureForProgressDto> lectures;
    }

    @Data
    @Builder
    public static class LectureForProgressDto {
        private Long lectureId;
        private Long testId;
    }
}
