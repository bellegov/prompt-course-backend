package com.promptcourse.progressservice.model;

import jakarta.persistence.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "user_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "lectureId"}))
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
