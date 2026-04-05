package com.promptcourse.ai_service.repository;

import com.promptcourse.ai_service.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUserIdOrderByCreatedAtDesc(Long userId);
}