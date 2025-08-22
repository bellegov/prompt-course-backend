package com.promptcourse.progressservice.repository;

import com.promptcourse.progressservice.model.UserGlobalProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserGlobalProgressRepository extends JpaRepository<UserGlobalProgress, Long> {
    Optional<UserGlobalProgress> findByUserId(Long userId);
}
