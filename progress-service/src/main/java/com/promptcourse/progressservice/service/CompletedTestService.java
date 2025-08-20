package com.promptcourse.progressservice.service;

import com.promptcourse.progressservice.model.CompletedTest;
import com.promptcourse.progressservice.repository.CompletedTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompletedTestService {

    private final CompletedTestRepository completedTestRepository;

    public void markTestAsCompleted(Long userId, Long testId) {
        if (!completedTestRepository.existsByUserIdAndTestId(userId, testId)) {
            CompletedTest completedTest = CompletedTest.builder()
                    .userId(userId)
                    .testId(testId)
                    .build();
            completedTestRepository.save(completedTest);
        }
    }
}
