package com.homework.backend.services;

import com.homework.backend.models.ChatMessage;
import com.homework.backend.models.Conversation;
import com.homework.backend.models.User;
import com.homework.backend.repositories.ChatMessageRepository;
import com.homework.backend.repositories.ConversationRepository;
import com.homework.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    // Injection des 3 repositories nécessaires
    public ChatService(ChatMessageRepository chatMessageRepository,
                       ConversationRepository conversationRepository,
                       UserRepository userRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @Transactional // Assure que toutes les opérations (recherche, création, sauvegarde) réussissent ou échouent ensemble
    public ChatMessage saveMessage(Long senderId, Long recipientId, String content) {

        // 1. Chercher si une conversation existe déjà entre ces deux utilisateurs
        Conversation conversation = conversationRepository.findConversationBetweenUsers(senderId, recipientId)
                .orElseGet(() -> {
                    // Si elle n'existe pas, on la crée.
                    User sender = userRepository.findById(senderId)
                            .orElseThrow(() -> new RuntimeException("Utilisateur expéditeur non trouvé : " + senderId));
                    User recipient = userRepository.findById(recipientId)
                            .orElseThrow(() -> new RuntimeException("Utilisateur destinataire non trouvé : " + recipientId));

                    Conversation newConversation = new Conversation();
                    newConversation.setUser1(sender);
                    newConversation.setUser2(recipient);
                    return conversationRepository.save(newConversation);
                });

        // 2. On récupère l'entité de l'expéditeur
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Utilisateur expéditeur non trouvé : " + senderId));

        // 3. On crée l'entité ChatMessage à partir des informations
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversation(conversation);
        chatMessage.setSender(sender);
        chatMessage.setContent(content);
        chatMessage.setTimestamp(LocalDateTime.now());

        // 4. On sauvegarde l'ENTITÉ ChatMessage, pas le DTO
        return chatMessageRepository.save(chatMessage);
    }
}
