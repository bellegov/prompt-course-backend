package com.promptcourse.progressservice.dto;

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
