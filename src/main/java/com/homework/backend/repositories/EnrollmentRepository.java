package com.homework.backend.repositories;

import com.homework.backend.models.Enrollment;
import com.homework.backend.models.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentIdAndCourseIdAndStatusIn(Long studentId, Long courseId, Collection<EnrollmentStatus> statuses);
    List<Enrollment> findByStudentId(Long studentId);
    long countByCourseIdAndStatusIn(Long courseId, Collection<EnrollmentStatus> statuses);

    @Query("""
            SELECT COUNT(DISTINCT e.student.id)
            FROM Enrollment e
            WHERE e.course.teacher.id = :teacherId
              AND e.status IN :statuses
            """)
    long countDistinctStudentsByTeacherIdAndStatusIn(@Param("teacherId") Long teacherId,
                                                     @Param("statuses") Collection<EnrollmentStatus> statuses);
}
