package com.homework.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatMessageDto(
        @NotNull
        Long recipientId,

        @NotBlank
        String content
) {
}
