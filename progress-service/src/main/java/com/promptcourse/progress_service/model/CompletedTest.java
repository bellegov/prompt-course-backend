package com.promptcourse.progress_service.model;
import jakarta.persistence.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "completed_tests", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "testId"}))
public class CompletedTest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long testId;
}
