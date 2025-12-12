package com.homework.backend.controllers;

import com.homework.backend.dto.CourseRequest;
import com.homework.backend.models.Category;
import com.homework.backend.models.Course;
import com.homework.backend.models.User;
import com.homework.backend.services.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Course> createCourse(@Valid @RequestBody CourseRequest courseRequest,
                                               @AuthenticationPrincipal User currentUser) {
        Course savedCourse = courseService.createCourse(courseRequest, currentUser);
        return ResponseEntity
                .created(URI.create("/courses/" + savedCourse.getId()))
                .body(savedCourse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }

    @GetMapping
    public ResponseEntity<Page<Course>> searchCourses(
            @RequestParam(required = false, name = "keyword") String keyword,
            @RequestParam(required = false, name = "title") String legacyTitle,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) String city,
            Pageable pageable) {

        String query = keyword != null ? keyword : legacyTitle;

        Page<Course> courses = courseService.searchCourses(query, category, city, pageable);
        return ResponseEntity.ok(courses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id,
                                               @Valid @RequestBody CourseRequest courseRequest,
                                               @AuthenticationPrincipal User currentUser) {
        Course updatedCourse = courseService.updateCourse(id, courseRequest, currentUser);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id,
                                             @AuthenticationPrincipal User currentUser) {
        courseService.deleteCourse(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getMyCourses(@AuthenticationPrincipal User currentUser) {
        List<Course> courses = courseService.getCoursesByTeacher(currentUser);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Course>> getAllCourses(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Category category,
            Pageable pageable
    ) {
        Page<Course> courses = courseService.findWithFilters(query, category, pageable);
        return ResponseEntity.ok(courses);
    }

}
