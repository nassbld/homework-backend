package com.homework.backend.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentRefundRequest(
        @NotNull(message = "L'inscription est obligatoire")
        Long enrollmentId
) {
}

