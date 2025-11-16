package com.homework.backend.dto;

import java.math.BigDecimal;

public record TeacherDashboardStatsResponse(
        long totalCourses,
        long activeStudents,
        BigDecimal monthlyRevenue,
        BigDecimal totalRevenue
) {
}

