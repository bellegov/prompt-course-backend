package com.promptcourse.progress_service.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_global_progress")
public class UserGlobalProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    // Храним порядковый номер (order_index) последнего завершенного раздела.
    // По умолчанию 0, что означает, что ни один раздел еще не пройден.
    @Builder.Default
    private int lastCompletedSectionOrderIndex = 0;
}
