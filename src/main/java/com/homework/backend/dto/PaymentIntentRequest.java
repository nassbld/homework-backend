package com.homework.backend.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentIntentRequest(
        @NotNull(message = "Le cours est obligatoire")
        Long courseId
) {
}

