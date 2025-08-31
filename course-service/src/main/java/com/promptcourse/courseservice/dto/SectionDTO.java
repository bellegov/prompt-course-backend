package com.promptcourse.courseservice.dto;
import lombok.Data;

@Data
public class SectionDTO {
    private String title;
    private String description;
    private int orderIndex;
    private Integer iconId;
}
