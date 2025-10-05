package com.homework.backend.dto;

import com.homework.backend.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Le prénom ne peut pas être vide")
        String firstName,

        @NotBlank(message = "Le nom ne peut pas être vide")
        String lastName,

        @NotBlank(message = "L'email ne peut pas être vide")
        @Email(message = "Le format de l'email est invalide")
        String email,

        @NotBlank(message = "Le mot de passe ne peut pas être vide")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        String password,

        @NotNull(message = "Le rôle est obligatoire")
        Role role
) {}
