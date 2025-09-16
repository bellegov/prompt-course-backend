package com.promptcourse.user_service.client;

import com.promptcourse.user_service.dto.course.UserPromptsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "COURSE-SERVICE")
public interface CourseServiceClient {
    @PostMapping("/internal/prompts/by-lectures")
    List<UserPromptsDto> getPromptsByLectures(@RequestBody List<Long> lectureIds);

    @PostMapping("/internal/cache/clear-outline")
    void clearOutlineCache(@RequestParam("userId") Long userId);
}
