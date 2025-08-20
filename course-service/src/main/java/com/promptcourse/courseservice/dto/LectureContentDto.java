package com.promptcourse.courseservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LectureContentDto {
    private Long id;
    private String title;
    private String contentText;
    private String videoUrl;
    private Long testId; // ID теста, если он есть, иначе null
}
