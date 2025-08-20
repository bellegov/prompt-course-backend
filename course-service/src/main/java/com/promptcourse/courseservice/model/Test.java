package com.promptcourse.courseservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "tests")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", unique = true)
    @JsonIgnore
    private Lecture lecture;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", unique = true)
    @JsonIgnore
    private Chapter chapter;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", unique = true)
    @JsonIgnore
    private Section section;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Question> questions;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    private int passingScore;
}