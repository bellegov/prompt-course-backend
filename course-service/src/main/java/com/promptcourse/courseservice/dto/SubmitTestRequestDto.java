package com.promptcourse.courseservice.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SubmitTestRequestDto {
    private Map<Long, Long> selectedAnswers;
}
