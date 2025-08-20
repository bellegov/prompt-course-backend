package com.promptcourse.courseservice.repository;


import com.promptcourse.courseservice.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findBySectionId(Long sectionId);
    List<Chapter> findBySectionIdOrderByOrderIndexAsc(Long sectionId);
}
