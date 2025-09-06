package com.promptcourse.userservice.dto;
import lombok.Data;
@Data
public class UpdateUserRequest {
    private String nickname;
    private String email;

}
