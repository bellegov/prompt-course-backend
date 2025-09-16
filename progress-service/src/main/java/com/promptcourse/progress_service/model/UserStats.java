package com.promptcourse.progress_service.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    // "Пройдено уроков": общее количество завершенных лекций
    @Builder.Default // Lombok установит значение по умолчанию при создании через builder
    private int totalLecturesCompleted = 0;

    // "Дней подряд": счетчик страйка
    @Builder.Default
    private int consecutiveDays = 0;

    // "Дней в приложении": общее количество уникальных дней
    @Builder.Default
    private int totalActiveDays = 0;

    // Дата последнего захода для расчета страйка
    private LocalDate lastLoginDate;
}
