package com.promptcourse.courseservice.dto.progress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttemptRequest {
    private Long userId;
    private Long testId;
}