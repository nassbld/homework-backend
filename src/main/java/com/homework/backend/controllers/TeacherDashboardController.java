package com.homework.backend.controllers;

import com.homework.backend.dto.TeacherDashboardStatsResponse;
import com.homework.backend.models.User;
import com.homework.backend.services.TeacherDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teachers/dashboard")
public class TeacherDashboardController {

    private final TeacherDashboardService teacherDashboardService;

    public TeacherDashboardController(TeacherDashboardService teacherDashboardService) {
        this.teacherDashboardService = teacherDashboardService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<TeacherDashboardStatsResponse> getStats(@AuthenticationPrincipal User teacher) {
        TeacherDashboardStatsResponse stats = teacherDashboardService.getStatsForTeacher(teacher);
        return ResponseEntity.ok(stats);
    }
}

