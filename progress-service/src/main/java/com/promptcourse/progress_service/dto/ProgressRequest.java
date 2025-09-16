package com.promptcourse.progress_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor      // <-- ДОБАВЬТЕ ЭТУ АННОТАЦИЮ
@AllArgsConstructor
public class ProgressRequest {
    private Long userId;
    private Long sectionId;
    private boolean isSubscribed;
}
