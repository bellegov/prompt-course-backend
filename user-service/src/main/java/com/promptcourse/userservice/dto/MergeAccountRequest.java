package com.promptcourse.userservice.dto;
import lombok.Data;
@Data
public class MergeAccountRequest {
    private String email;
    private String password;
}
