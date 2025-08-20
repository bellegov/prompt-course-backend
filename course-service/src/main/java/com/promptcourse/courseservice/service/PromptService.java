package com.promptcourse.courseservice.service;

import com.promptcourse.courseservice.dto.UserPromptsDto;
import com.promptcourse.courseservice.model.Lecture;
import com.promptcourse.courseservice.model.Prompt;
import com.promptcourse.courseservice.repository.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromptService {
    private final LectureRepository lectureRepository;

    public List<UserPromptsDto> getPromptsForLectures(List<Long> lectureIds) {
        // Находим все лекции по списку ID
        List<Lecture> lectures = lectureRepository.findAllById(lectureIds);

        // Группируем лекции по их разделам, затем собираем промпты
        return lectures.stream()
                .collect(Collectors.groupingBy(l -> l.getChapter().getSection()))
                .entrySet().stream()
                .map(entry -> UserPromptsDto.builder()
                        .sectionId(entry.getKey().getId())
                        .sectionTitle(entry.getKey().getTitle())
                        .prompts(entry.getValue().stream() // Берем все лекции в этой группе
                                .flatMap(l -> l.getPrompts().stream()) // Собираем все промпты из этих лекций
                                .map(prompt -> UserPromptsDto.PromptData.builder()
                                        .id(prompt.getId())
                                        .title(prompt.getTitle())
                                        .text(prompt.getPromptText())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
