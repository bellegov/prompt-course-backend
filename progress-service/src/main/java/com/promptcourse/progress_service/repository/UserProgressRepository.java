package com.promptcourse.progress_service.repository;
import com.promptcourse.progress_service.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Set;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserIdAndSectionId(Long userId, Long sectionId);
    Set<UserProgress> findByUserIdAndLectureIdIn(Long userId, Set<Long> lectureIds);
    List<UserProgress> findByUserId(Long userId);
}
