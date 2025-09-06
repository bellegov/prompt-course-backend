package com.promptcourse.courseservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateTestRequest {
    private String title;
    private List<QuestionDTO> questions;
    private Integer passingScore;

    @Data
    public static class QuestionDTO {
        private String questionText;
        private List<AnswerDTO> answers;
    }

    @Data
    public static class AnswerDTO {
        private String answerText;
        private Boolean isCorrect;
    }
}