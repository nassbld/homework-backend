package com.homework.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ConversationSnippetDto(
        @NotNull
        Long conversationId,

        @NotNull
        String otherUserFirstName,

        @NotNull
        String otherUserLastName,

        @NotNull
        Long otherUserId,

        @NotNull
        String lastMessageContent,

        @NotNull
        LocalDateTime lastMessageTimestamp,

        Long lastMessageSenderId
) {
}
