package com.promptcourse.progressservice.service;
import com.promptcourse.progressservice.model.UserGlobalProgress;
import com.promptcourse.progressservice.repository.UserGlobalProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GlobalProgressService {

    private final UserGlobalProgressRepository globalProgressRepository;

    public UserGlobalProgress getGlobalProgress(Long userId) {
        // Находим прогресс пользователя или создаем новый, если это его первый визит
        return globalProgressRepository.findByUserId(userId)
                .orElseGet(() -> globalProgressRepository.save(
                        UserGlobalProgress.builder().userId(userId).build()
                ));
    }

    public void completeSection(Long userId, int sectionOrderIndex) {
        UserGlobalProgress progress = getGlobalProgress(userId);
        // Обновляем, только если новый пройденный раздел идет по порядку
        if (sectionOrderIndex > progress.getLastCompletedSectionOrderIndex()) {
            progress.setLastCompletedSectionOrderIndex(sectionOrderIndex);
            globalProgressRepository.save(progress);
        }
    }
}
