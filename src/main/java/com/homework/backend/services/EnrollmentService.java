package com.homework.backend.services;

import com.homework.backend.dto.EnrollmentResponse;
import com.homework.backend.models.Course;
import com.homework.backend.models.Enrollment;
import com.homework.backend.models.EnrollmentStatus;
import com.homework.backend.models.User;
import com.homework.backend.repositories.CourseRepository;
import com.homework.backend.repositories.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public Enrollment createEnrollment(Long courseId, User student) {
        boolean hasActiveEnrollment = enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
                student.getId(),
                courseId,
                List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED)
        );
        if (hasActiveEnrollment) {
            throw new IllegalStateException("Vous êtes déjà inscrit à ce cours.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Cours non trouvé."));

        if (course.getTeacher().getId().equals(student.getId())) {
            throw new IllegalStateException("Un formateur ne peut pas s'inscrire à son propre cours.");
        }

        if (course.getMaxStudents() != null) {
            long currentEnrollmentCount = enrollmentRepository.countByCourseIdAndStatusIn(
                    courseId,
                    List.of(EnrollmentStatus.ACTIVE)
            );
            if (currentEnrollmentCount >= course.getMaxStudents()) {
                throw new IllegalStateException("Le cours est complet. Impossible de s'inscrire.");
            }
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        return enrollmentRepository.save(enrollment);
    }

    public List<EnrollmentResponse> getEnrollmentsForStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        Course course = enrollment.getCourse();
        User teacher = course.getTeacher();

        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus(),
                new EnrollmentResponse.CourseSummary(
                        course.getId(),
                        course.getTitle(),
                        course.getDescription(),
                        course.getCategory(),
                        course.getPrice(),
                        course.getCity(),
                        course.getCourseDateTime(),
                        course.getDuration(),
                        course.getMaxStudents(),
                        course.getEnrolledStudentsCount(),
                        course.getCreatedAt(),
                        new EnrollmentResponse.TeacherSummary(
                                teacher.getId(),
                                teacher.getFirstName(),
                                teacher.getLastName(),
                                teacher.getEmail(),
                                teacher.getBio(),
                                teacher.getRole()
                        )
                )
        );
    }
}
