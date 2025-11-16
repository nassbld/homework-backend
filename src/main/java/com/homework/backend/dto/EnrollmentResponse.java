package com.homework.backend.dto;

import com.homework.backend.models.Category;
import com.homework.backend.models.EnrollmentStatus;
import com.homework.backend.models.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        LocalDateTime enrolledAt,
        EnrollmentStatus status,
        CourseSummary course
) {

    public record CourseSummary(
            Long id,
            String title,
            String description,
            Category category,
            BigDecimal price,
            String city,
            LocalDateTime courseDateTime,
            Integer duration,
            Integer maxStudents,
            int enrolledStudentsCount,
            LocalDateTime createdAt,
            TeacherSummary teacher
    ) {
    }

    public record TeacherSummary(
            Long id,
            String firstName,
            String lastName,
            String email,
            String bio,
            Role role
    ) {
    }
}

