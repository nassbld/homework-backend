package com.homework.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentConfirmationRequest(
        @NotBlank(message = "L'identifiant de paiement est obligatoire")
        String paymentIntentId
) {
}

