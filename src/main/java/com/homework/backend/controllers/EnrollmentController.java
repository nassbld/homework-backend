package com.homework.backend.controllers;

import com.homework.backend.dto.EnrollmentRequest;
import com.homework.backend.dto.EnrollmentResponse;
import com.homework.backend.models.Enrollment;
import com.homework.backend.models.User;
import com.homework.backend.services.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<EnrollmentResponse> enrollInCourse(@RequestBody EnrollmentRequest request, @AuthenticationPrincipal User student) {
        Enrollment newEnrollment = enrollmentService.createEnrollment(request.courseId(), student);
        return ResponseEntity.ok(enrollmentService.toResponse(newEnrollment));
    }

    @GetMapping("/my-courses")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> getMyCourses(@AuthenticationPrincipal User student) {
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsForStudent(student.getId());
        return ResponseEntity.ok(enrollments);
    }
}
