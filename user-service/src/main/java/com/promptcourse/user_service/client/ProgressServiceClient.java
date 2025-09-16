package com.promptcourse.user_service.client;

import com.promptcourse.user_service.dto.progress.UserStats;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

@FeignClient(name = "PROGRESS-SERVICE")
public interface ProgressServiceClient {

    @GetMapping("/internal/stats/users/{userId}")
    UserStats getStatsForUser(@PathVariable("userId") Long userId);

    @GetMapping("/internal/users/{userId}/completed-lectures")
    Set<Long> getCompletedLectureIds(@PathVariable("userId") Long userId);
}
