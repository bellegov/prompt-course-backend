package com.promptcourse.courseservice.dto;

// Этот Enum дублирует такой же в progress-service для удобства межсервисного общения
public enum LectureState {
    LOCKED,   // Заблокирована (серая)
    UNLOCKED, // Текущая активная (можно проходить)
    COMPLETED // Пройдена (раскрашена)
}
