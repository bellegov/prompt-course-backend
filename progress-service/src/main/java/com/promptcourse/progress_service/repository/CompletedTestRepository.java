package com.promptcourse.progress_service.repository;
import com.promptcourse.progress_service.model.CompletedTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompletedTestRepository extends JpaRepository<CompletedTest, Long> {
    boolean existsByUserIdAndTestId(Long userId, Long testId);
    Optional<CompletedTest> findByUserIdAndTestId(Long userId, Long testId);
}
