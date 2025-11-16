package com.homework.backend.services;

import com.homework.backend.dto.TeacherDashboardStatsResponse;
import com.homework.backend.models.EnrollmentStatus;
import com.homework.backend.models.PaymentStatus;
import com.homework.backend.models.User;
import com.homework.backend.repositories.CourseRepository;
import com.homework.backend.repositories.EnrollmentRepository;
import com.homework.backend.repositories.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TeacherDashboardService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PaymentRepository paymentRepository;

    public TeacherDashboardService(CourseRepository courseRepository,
                                   EnrollmentRepository enrollmentRepository,
                                   PaymentRepository paymentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.paymentRepository = paymentRepository;
    }

    public TeacherDashboardStatsResponse getStatsForTeacher(User teacher) {
        long totalCourses = courseRepository.countByTeacherId(teacher.getId());

        long activeStudents = enrollmentRepository.countDistinctStudentsByTeacherIdAndStatusIn(
                teacher.getId(),
                List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED)
        );

        BigDecimal totalRevenue = paymentRepository.sumTeacherAmountByTeacherIdAndStatus(
                teacher.getId(),
                PaymentStatus.SUCCEEDED
        );
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime startOfNextMonth = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        BigDecimal monthlyRevenue = paymentRepository.sumTeacherAmountByTeacherIdAndStatusBetween(
                teacher.getId(),
                PaymentStatus.SUCCEEDED,
                startOfMonth,
                startOfNextMonth
        );
        if (monthlyRevenue == null) {
            monthlyRevenue = BigDecimal.ZERO;
        }

        return new TeacherDashboardStatsResponse(
                totalCourses,
                activeStudents,
                monthlyRevenue,
                totalRevenue
        );
    }
}

