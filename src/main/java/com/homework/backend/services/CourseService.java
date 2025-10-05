package com.homework.backend.services;

import com.homework.backend.dto.CourseRequest;
import com.homework.backend.models.Category;
import com.homework.backend.models.Course;
import com.homework.backend.models.Role;
import com.homework.backend.models.User;
import com.homework.backend.repositories.CourseRepository;
import com.homework.backend.specifications.CourseSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Course createCourse(CourseRequest courseRequest, User currentUser) {
        if (currentUser.getRole() != Role.TEACHER) {
            throw new SecurityException("Seul un formateur peut créer un cours.");
        }

        Course course = Course.builder()
                .title(courseRequest.title())
                .description(courseRequest.description())
                .category(courseRequest.category())
                .pricePerHour(courseRequest.pricePerHour())
                .city(courseRequest.city())
                .teacher(currentUser)
                .duration(courseRequest.duration())
                .maxStudents(courseRequest.maxStudents())
                .build();

        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public Page<Course> searchCourses(String title, Category category, String city, Pageable pageable) {
        Specification<Course> spec = Specification.allOf();

        if (title != null && !title.isEmpty()) {
            spec = spec.and(CourseSpecification.titleContains(title));
        }
        if (category != null) {
            spec = spec.and(CourseSpecification.hasCategory(category));
        }
        if (city != null && !city.isEmpty()) {
            spec = spec.and(CourseSpecification.cityContains(city));
        }

        return courseRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cours non trouvé avec l'ID : " + id));
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByTeacher(User teacher) {
        return courseRepository.findByTeacher(teacher);
    }

    @Transactional
    public Course updateCourse(Long id, CourseRequest courseRequest, User currentUser) {
        Course courseToUpdate = getCourseById(id);

        // Vérification : seul le propriétaire du cours peut le modifier
        if (!courseToUpdate.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier ce cours.");
        }

        courseToUpdate.setTitle(courseRequest.title());
        courseToUpdate.setDescription(courseRequest.description());
        courseToUpdate.setCategory(courseRequest.category());
        courseToUpdate.setPricePerHour(courseRequest.pricePerHour());
        courseToUpdate.setCity(courseRequest.city());
        courseToUpdate.setDuration(courseRequest.duration());
        courseToUpdate.setMaxStudents(courseRequest.maxStudents());

        return courseRepository.save(courseToUpdate);
    }

    @Transactional
    public void deleteCourse(Long id, User currentUser) {
        Course courseToDelete = getCourseById(id);

        // Vérification : seul le propriétaire du cours peut le supprimer
        if (!courseToDelete.getTeacher().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer ce cours.");
        }

        courseRepository.delete(courseToDelete);
    }

    @Transactional(readOnly = true)
    public Page<Course> findWithFilters(String query, Category category, Pageable pageable) {
        // On commence avec une spécification qui ne filtre rien
        Specification<Course> spec = Specification.where(null);

        // Si un mot-clé de recherche est fourni, on ajoute une condition "LIKE" sur le titre
        if (query != null && !query.isEmpty()) {
            spec = spec.and((root, q, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + query.toLowerCase() + "%")
            );
        }

        // Si une catégorie est fournie, on ajoute une condition "EQUAL"
        if (category != null) {
            spec = spec.and((root, q, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category"), category)
            );
        }

        // On exécute la recherche avec les spécifications construites
        return courseRepository.findAll(spec, pageable);
    }
}
