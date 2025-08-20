package com.promptcourse.courseservice.dto.progress;
import lombok.AllArgsConstructor; // <-- ДОБАВЬТЕ ЭТОТ ИМПОРТ
import lombok.Data;
import lombok.NoArgsConstructor;   // <-- ДОБАВЬТЕ ЭТОТ ИМПОРТ

@Data
@NoArgsConstructor      // <-- ДОБАВЬТЕ ЭТУ АННОТАЦИЮ
@AllArgsConstructor     // <-- ДОБАВЬТЕ ЭТУ АННОТАЦИЮ
public class ProgressRequest {
    private Long userId;
    private Long sectionId;
    private boolean isSubscribed;
}
