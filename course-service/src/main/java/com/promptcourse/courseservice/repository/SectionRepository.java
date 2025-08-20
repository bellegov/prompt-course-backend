package com.promptcourse.courseservice.repository;


import com.promptcourse.courseservice.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findAllByOrderByOrderIndexAsc();
}
