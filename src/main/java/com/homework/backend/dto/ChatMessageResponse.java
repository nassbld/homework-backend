// Nouveau DTO pour la réponse WebSocket
package com.homework.backend.dto;

import com.homework.backend.models.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long senderId,
        String senderFirstName,
        String senderLastName,
        String content,
        LocalDateTime timestamp
) {
    public static ChatMessageResponse fromEntity(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getFirstName(),
                message.getSender().getLastName(),
                message.getContent(),
                message.getTimestamp()
        );
    }
}
