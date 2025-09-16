package com.promptcourse.user_service.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPromptsDto {
    private Long sectionId;
    private String sectionTitle;
    private List<PromptData> prompts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptData {
        private Long id;
        private String title;
        private String text;
    }
}
