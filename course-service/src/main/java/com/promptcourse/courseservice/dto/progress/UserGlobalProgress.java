package com.promptcourse.courseservice.dto.progress;

import lombok.Data;

// Это DTO, а не сущность
@Data
public class UserGlobalProgress {
    private Long id;
    private Long userId;
    private int lastCompletedSectionOrderIndex;
}
