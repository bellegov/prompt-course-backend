package com.promptcourse.progress_service.repository;
import com.promptcourse.progress_service.model.UserLifeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserLifeStatusRepository extends JpaRepository<UserLifeStatus, Long> {
    Optional<UserLifeStatus> findByUserId(Long userId);
}
