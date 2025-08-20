package com.promptcourse.courseservice.repository;

import com.promptcourse.courseservice.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestRepository extends JpaRepository<Test,Long> {
    Optional<Test> findByLectureId(Long lectureId);
    Optional<Test> findByChapterId(Long chapterId);

    Optional<Test> findBySectionId(Long sectionId);
}
