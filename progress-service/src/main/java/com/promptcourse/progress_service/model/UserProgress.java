package com.promptcourse.progress_service.model;

import jakarta.persistence.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
// Добавляем аннотацию @Table с определением индексов
@Table(name = "user_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "lectureId"}),
        indexes = {
                @Index(name = "idx_userprogress_userid", columnList = "userId"),
                @Index(name = "idx_userprogress_user_section", columnList = "userId, sectionId")
        }
)
public class UserProgress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long lectureId;
    @Column(nullable = false)
    private Long sectionId;
}
