package com.promptcourse.courseservice.repository;


import com.promptcourse.courseservice.model.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptRepository extends JpaRepository<Prompt, Long> {
}