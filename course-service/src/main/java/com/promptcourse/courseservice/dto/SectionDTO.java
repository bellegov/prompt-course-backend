package com.promptcourse.courseservice.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SectionDTO {
    private String title;
    private String description;
    private int orderIndex;
    private Integer iconId;

    @JsonProperty("isPremium")
    private boolean isPremium;
}
