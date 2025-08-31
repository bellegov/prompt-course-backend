package com.promptcourse.courseservice.dto;

import java.io.Serializable;

// Этот Enum дублирует такой же в progress-service для удобства межсервисного общения
public enum LectureState implements Serializable {
    LOCKED,   // Заблокирована (серая)
    UNLOCKED, // Текущая активная (можно проходить)
    COMPLETED // Пройдена (раскрашена)
}
