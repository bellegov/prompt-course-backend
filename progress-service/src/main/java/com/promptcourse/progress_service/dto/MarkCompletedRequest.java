package com.promptcourse.progress_service.dto;

import lombok.Data;
@Data
public class MarkCompletedRequest {
    private Long userId;
    private Long lectureId;
    private Long sectionId;
}
