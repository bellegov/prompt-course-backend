package com.promptcourse.progressservice.repository;
import com.promptcourse.progressservice.model.UserLifeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserLifeStatusRepository extends JpaRepository<UserLifeStatus, Long> {
    Optional<UserLifeStatus> findByUserId(Long userId);
}
