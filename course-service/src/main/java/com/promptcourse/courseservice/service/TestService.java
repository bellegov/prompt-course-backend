package com.promptcourse.courseservice.service;
import com.promptcourse.courseservice.client.ProgressServiceClient;
import com.promptcourse.courseservice.dto.SubmitTestRequestDto;
import com.promptcourse.courseservice.dto.TestResultDto;
import com.promptcourse.courseservice.dto.UserTestViewDto;
import com.promptcourse.courseservice.dto.progress.AttemptRequest;
import com.promptcourse.courseservice.dto.progress.AttemptStatusResponse;
import com.promptcourse.courseservice.model.Answer;
import com.promptcourse.courseservice.model.Question;
import com.promptcourse.courseservice.model.Test;
import com.promptcourse.courseservice.repository.TestRepository;
import org.slf4j.Logger; // <-- Новый импорт
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final ProgressServiceClient progressServiceClient;
    private static final Logger log = LoggerFactory.getLogger(TestService.class);

    public UserTestViewDto getTestForUser(Long testId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        return mapTestToViewDto(test);
    }

    public TestResultDto submitTest(Long testId, Long userId, boolean isSubscribed, SubmitTestRequestDto submission) {

        // 1. ПРОВЕРКА РАЗРЕШЕНИЯ
        AttemptStatusResponse status = progressServiceClient.getAttemptStatus(userId, isSubscribed);

        if (!status.isCanAttempt()) {
            throw new IllegalStateException(status.getMessage());
        }

        // 2. ПРОВЕРКА ОТВЕТОВ
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        int correctAnswersCount = 0;
        for (Question question : test.getQuestions()) {
            Long correctAnwerId = question.getAnswers().stream()
                    .filter(Answer::isCorrect)
                    .findFirst()
                    .map(Answer::getId)
                    .orElse(null);

            Object userAnswerValue = submission.getSelectedAnswers().get(question.getId());
            if (correctAnwerId != null && userAnswerValue != null) {
                try {
                    Long userAnswerId = Long.valueOf(userAnswerValue.toString());
                    if (correctAnwerId.equals(userAnswerId)) {
                        correctAnswersCount++;
                    }
                } catch (NumberFormatException e) {
                    log.warn("Could not parse user answer '{}' to Long.", userAnswerValue);
                }
            }
        }

        int totalQuestions = test.getQuestions().size();
        boolean passed = correctAnswersCount >= test.getPassingScore();

        // 3. ЗАПИСЬ РЕЗУЛЬТАТА
        if (passed) {
            // Если тест сдан, СООБЩАЕМ progress-service, чтобы он создал "временный пропуск"
            log.info("Test passed for userId: {}. Recording success for testId: {}", userId, testId);
            progressServiceClient.recordSuccess(new AttemptRequest(userId, testId));
        } else {
            // Если тест не сдан, СООБЩАЕМ progress-service, чтобы он списал жизнь
            log.info("Test failed for userId: {}. Recording failure.", userId);
            progressServiceClient.recordFailure(userId, isSubscribed);
        }

        return TestResultDto.builder()
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswersCount)
                .passed(passed)
                .message(passed ? "Поздравляем, тест пройден!" : "Вы не набрали достаточное количество баллов.")
                .build();
    }


    private UserTestViewDto mapTestToViewDto(Test test) {
        return UserTestViewDto.builder()
                .testId(test.getId())
                .title(test.getTitle())
                .questions(test.getQuestions().stream()
                        .map(this::mapQuestionToViewDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private UserTestViewDto.QuestionViewDto mapQuestionToViewDto(Question question) {
        return UserTestViewDto.QuestionViewDto.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .answers(question.getAnswers().stream()
                        .map(this::mapAnswerToViewDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private UserTestViewDto.AnswerViewDto mapAnswerToViewDto(Answer answer) {
        return UserTestViewDto.AnswerViewDto.builder()
                .answerId(answer.getId())
                .answerText(answer.getAnswerText())
                .build();
    }
}