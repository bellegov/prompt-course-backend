package com.promptcourse.courseservice.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestResultDto {
    private int totalQuestions;
    private int correctAnswers;
    private boolean passed;
    private String message;
}
