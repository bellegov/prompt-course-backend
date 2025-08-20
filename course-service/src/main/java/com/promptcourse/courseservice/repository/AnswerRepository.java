package com.promptcourse.courseservice.repository;

import com.promptcourse.courseservice.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer,Long> {
}
