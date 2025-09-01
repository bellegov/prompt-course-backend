package com.promptcourse.courseservice.listener;
import com.promptcourse.courseservice.dto.event.CacheInvalidationEvent; // <-- DTO для события
import com.promptcourse.courseservice.service.CourseViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationListener {

    private final CourseViewService courseViewService;

    // Этот метод будет АВТОМАТИЧЕСКИ вызываться каждый раз,
    // когда в топик "cache-invalidation" приходит новое сообщение.
    @KafkaListener(topics = "cache-invalidation", groupId = "course-service-group")
    public void handleCacheInvalidation(CacheInvalidationEvent event) {
        log.info("Received cache invalidation event for userId: {}", event.getUserId());
        courseViewService.clearOutlineCacheForUser(event.getUserId());
    }
}
