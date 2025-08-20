package com.promptcourse.courseservice.controller;
import com.promptcourse.courseservice.dto.UserPromptsDto;
import com.promptcourse.courseservice.service.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/internal/prompts")
@RequiredArgsConstructor
public class InternalPromptController {

    private final PromptService promptService;

    @PostMapping("/by-lectures")
    public ResponseEntity<List<UserPromptsDto>> getPromptsByLectures(@RequestBody List<Long> lectureIds) {
        return ResponseEntity.ok(promptService.getPromptsForLectures(lectureIds));
    }
}
