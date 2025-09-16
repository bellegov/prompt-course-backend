package com.promptcourse.user_service.dto;
import lombok.Data;
@Data
public class LinkAccountRequest {
    private String email;
    private String password;
}
