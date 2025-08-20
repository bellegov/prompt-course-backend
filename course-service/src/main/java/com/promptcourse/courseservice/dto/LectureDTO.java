package com.promptcourse.courseservice.dto;


import lombok.Data;

@Data
public class LectureDTO {
    private String title;
    private String contentText;
    private String videoUrl;
    private int orderIndex;
}
