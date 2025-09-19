package com.promptcourse.progress_service.service;

import com.promptcourse.progress_service.client.CourseServiceClient;
import com.promptcourse.progress_service.model.CompletedTest;
import com.promptcourse.progress_service.repository.CompletedTestRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompletedTestService {

    private final CompletedTestRepository completedTestRepository;
    private final CourseServiceClient courseServiceClient;
    private static final Logger log = LoggerFactory.getLogger(CompletedTestService.class);


    public void markTestAsCompleted(Long userId, Long testId) {
        if (!completedTestRepository.existsByUserIdAndTestId(userId, testId)) {
            CompletedTest completedTest = CompletedTest.builder()
                    .userId(userId)
                    .testId(testId)
                    .build();
            completedTestRepository.save(completedTest);
        }
        // --- СБРАСЫВАЕМ КЭШ ПОСЛЕ УСПЕШНОЙ СДАЧИ ---
        try {
            courseServiceClient.clearOutlineCache(userId);
            log.info("Cache invalidation requested for userId {} after test success.", userId);
        } catch (Exception e) {
            log.error("Failed to invalidate cache for userId {} after test success.", userId, e);
        }
    }
    }

