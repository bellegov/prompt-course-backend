package com.promptcourse.courseservice.repository;

import com.promptcourse.courseservice.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findByChapterId(Long chapterId);
    List<Lecture> findByChapterIdOrderByOrderIndexAsc(Long chapterId);
}
