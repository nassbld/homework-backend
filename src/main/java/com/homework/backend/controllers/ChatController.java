package com.homework.backend.controllers;

import com.homework.backend.dto.ChatMessageDto;
import com.homework.backend.dto.ChatMessageResponse;
import com.homework.backend.models.ChatMessage;
import com.homework.backend.models.User;
import com.homework.backend.services.ChatService;
import com.homework.backend.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate, ChatService chatService, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat")
    public void processMessage(
            @Payload @Valid ChatMessageDto chatMessageDto,
            Principal principal
    ) {
        System.out.println("📩 Message reçu de : " + principal.getName());

        String userEmail = principal.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé: " + userEmail));

        ChatMessage savedMessage = chatService.saveMessage(currentUser.getId(), chatMessageDto.recipientId(), chatMessageDto.content());

        ChatMessageResponse responseDto = ChatMessageResponse.fromEntity(savedMessage);

        User recipient = userRepository.findById(chatMessageDto.recipientId())
                .orElseThrow(() -> new IllegalStateException("Destinataire non trouvé"));

        System.out.println("📤 Envoi au destinataire : " + recipient.getEmail());
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(), "/queue/messages", responseDto);

        System.out.println("📤 Envoi à l'expéditeur : " + currentUser.getEmail());
        messagingTemplate.convertAndSendToUser(
                currentUser.getEmail(), "/queue/messages", responseDto);
    }

}
