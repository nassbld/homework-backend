package com.homework.backend.specifications;

import com.homework.backend.models.Category;
import com.homework.backend.models.Course;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class CourseSpecification {

    private static String likePattern(String value) {
        return "%" + value.toLowerCase() + "%";
    }

    public static Specification<Course> matchesKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            var teacherJoin = root.join("teacher", JoinType.LEFT);
            String pattern = likePattern(keyword);

            var titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    pattern
            );

            var descriptionExpression = criteriaBuilder.coalesce(
                    root.get("description").as(String.class),
                    ""
            );
            var descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(descriptionExpression),
                    pattern
            );

            var cityPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("city")),
                    pattern
            );

            var teacherFirstNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(teacherJoin.get("firstName")),
                    pattern
            );
            var teacherLastNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(teacherJoin.get("lastName")),
                    pattern
            );

            return criteriaBuilder.or(
                    titlePredicate,
                    descriptionPredicate,
                    cityPredicate,
                    teacherFirstNamePredicate,
                    teacherLastNamePredicate
            );
        };
    }

    public static Specification<Course> hasCategory(Category category) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Course> cityContains(String city) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), likePattern(city));
    }
}
