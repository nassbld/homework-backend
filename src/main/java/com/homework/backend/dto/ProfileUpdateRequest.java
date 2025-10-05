package com.homework.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequest(
        @NotBlank(message = "Le prénom ne peut pas être vide")
        String firstName,

        @NotBlank(message = "Le nom ne peut pas être vide")
        String lastName,

        String bio
) {
}
