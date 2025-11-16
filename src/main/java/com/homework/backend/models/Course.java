package com.homework.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "price_per_course", nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String city;

    @Column(name = "course_date_time")
    private LocalDateTime courseDateTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Formula("(SELECT COUNT(e.id) FROM enrollments e WHERE e.course_id = id)")
    private int enrolledStudentsCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
}
