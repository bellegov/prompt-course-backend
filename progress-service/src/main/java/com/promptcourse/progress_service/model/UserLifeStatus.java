package com.promptcourse.progress_service.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_life_statuses")
public class UserLifeStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Уникальный ID пользователя, один к одному
    @Column(unique = true, nullable = false)
    private Long userId;

    // Текущее количество жизней
    @Column(nullable = false)
    private int lives;

    // Время, когда начнется восстановление.
    // Устанавливается, когда пользователь тратит первую жизнь.
    private LocalDateTime recoveryStartedAt;
}
