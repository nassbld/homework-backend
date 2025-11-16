package com.homework.backend.repositories;

import com.homework.backend.models.Course;
import com.homework.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    List<Course> findByTeacher(User teacher);
    long countByTeacherId(Long teacherId);
}
