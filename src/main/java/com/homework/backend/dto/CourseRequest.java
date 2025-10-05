package com.homework.backend.dto;

import com.homework.backend.models.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CourseRequest(
        @NotEmpty(message = "Le titre ne peut pas être vide")
        String title,

        String description,

        @NotNull(message = "La catégorie est obligatoire")
        Category category,

        @NotNull(message = "Le prix ne peut pas être nul")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
        BigDecimal pricePerHour,

        @NotEmpty(message = "La ville ne peut pas être vide")
        String city,

        @Min(value = 1, message = "La durée doit être d'au moins 1 minute.")
        Integer duration,

        @Min(value = 1, message = "La capacité maximale doit être d'au moins 1 élève.")
        Integer maxStudents
) {
}
