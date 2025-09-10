package com.promptcourse.progressservice.client;

import com.promptcourse.progressservice.dto.SectionForProgressDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "COURSE-SERVICE")
public interface CourseServiceClient {
    @GetMapping("/internal/courses/sections/{sectionId}/structure")
    SectionForProgressDto getSectionStructure(@PathVariable("sectionId") Long sectionId);

    // --- НОВЫЙ МЕТОД ---
    @GetMapping("/internal/courses/lectures/{lectureId}/info")
    SectionForProgressDto.LectureForProgressDto getLectureInfo(@PathVariable("lectureId") Long lectureId);

    @PostMapping("/internal/cache/clear-outline")
    void clearOutlineCache(@RequestParam("userId") Long userId, @RequestParam("isSubscribed") boolean isSubscribed);
}
