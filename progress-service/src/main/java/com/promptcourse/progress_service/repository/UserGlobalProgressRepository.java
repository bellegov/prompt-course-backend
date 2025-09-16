package com.promptcourse.progress_service.repository;

import com.promptcourse.progress_service.model.UserGlobalProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserGlobalProgressRepository extends JpaRepository<UserGlobalProgress, Long> {
    Optional<UserGlobalProgress> findByUserId(Long userId);
}
