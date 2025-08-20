package com.promptcourse.progressservice.dto;

import lombok.Data;
@Data
public class MarkCompletedRequest {
    private Long userId;
    private Long lectureId;
    private Long sectionId;
}
