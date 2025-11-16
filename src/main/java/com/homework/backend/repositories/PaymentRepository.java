package com.homework.backend.repositories;

import com.homework.backend.models.Payment;
import com.homework.backend.models.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
    Optional<Payment> findByEnrollmentId(Long enrollmentId);
    Optional<Payment> findByIdAndStudentId(Long paymentId, Long studentId);
    boolean existsByCourseIdAndStudentIdAndStatusIn(Long courseId, Long studentId, Collection<com.homework.backend.models.PaymentStatus> statuses);

    @Query("""
            SELECT COALESCE(SUM(p.teacherAmount), 0)
            FROM Payment p
            WHERE p.course.teacher.id = :teacherId
              AND p.status = :status
            """)
    BigDecimal sumTeacherAmountByTeacherIdAndStatus(@Param("teacherId") Long teacherId,
                                                    @Param("status") PaymentStatus status);

    @Query("""
            SELECT COALESCE(SUM(p.teacherAmount), 0)
            FROM Payment p
            WHERE p.course.teacher.id = :teacherId
              AND p.status = :status
              AND p.createdAt >= :start
              AND p.createdAt < :end
            """)
    BigDecimal sumTeacherAmountByTeacherIdAndStatusBetween(@Param("teacherId") Long teacherId,
                                                           @Param("status") PaymentStatus status,
                                                           @Param("start") LocalDateTime start,
                                                           @Param("end") LocalDateTime end);
}

