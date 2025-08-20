package com.promptcourse.courseservice.dto;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserPromptsDto {
    private Long sectionId;
    private String sectionTitle;
    private List<PromptData> prompts; // <-- Тип изменен на вложенный класс

    @Data
    @Builder
    public static class PromptData {
        private Long id;
        private String title;
        private String text;
    }
}
