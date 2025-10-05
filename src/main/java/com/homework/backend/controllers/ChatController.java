package com.homework.backend.controllers;

import com.homework.backend.dto.ChatMessageDto;
import com.homework.backend.models.ChatMessage;
import com.homework.backend.models.User;
import com.homework.backend.services.ChatService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public ChatController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat")
    public void processMessage(
            @Payload @Valid ChatMessageDto chatMessageDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new IllegalStateException("UserDetails est null, l'authentification a échoué.");
        }

        User currentUser = (User) userDetails;
        Long senderId = currentUser.getId();

        ChatMessage savedMessage = chatService.saveMessage(senderId, chatMessageDto.recipientId(), chatMessageDto.content());

        messagingTemplate.convertAndSendToUser(
                chatMessageDto.recipientId().toString(), "/queue/messages", savedMessage);
        messagingTemplate.convertAndSendToUser(
                senderId.toString(), "/queue/messages", savedMessage);
    }
}
