package com.promptcourse.courseservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserTestViewDto {
    private Long testId;
    private String title;
    private List<QuestionViewDto> questions;

    @Data
    @Builder
    public static class QuestionViewDto {
        private Long questionId;
        private String questionText;
        private List<AnswerViewDto> answers;
    }

    @Data
    @Builder
    public static class AnswerViewDto {
        private Long answerId;
        private String answerText;
    }
}
