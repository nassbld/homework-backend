package com.homework.backend.services;

import com.homework.backend.dto.ConversationSnippetDto;
import com.homework.backend.models.ChatMessage;
import com.homework.backend.models.Conversation;
import com.homework.backend.models.User;
import com.homework.backend.repositories.ChatMessageRepository;
import com.homework.backend.repositories.ConversationRepository;
import com.homework.backend.repositories.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ConversationService(ConversationRepository conversationRepository,
                               ChatMessageRepository chatMessageRepository,
                               UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Récupère un résumé de toutes les conversations pour un utilisateur donné.
     */
    public List<ConversationSnippetDto> findConversationsForUser(Long userId) {
        List<Conversation> conversations = conversationRepository.findByUser1IdOrUser2Id(userId, userId);

        return conversations.stream()
                .map(conv -> {
                    User otherUser = conv.getUser1().getId().equals(userId) ? conv.getUser2() : conv.getUser1();
                    ChatMessage lastMessage = chatMessageRepository.findTopByConversationOrderByTimestampDesc(conv);

                    return new ConversationSnippetDto(
                            conv.getId(),
                            otherUser.getFirstName(),
                            otherUser.getLastName(),
                            otherUser.getId(),
                            lastMessage != null ? lastMessage.getContent() : "Début de la conversation",
                            lastMessage != null ? lastMessage.getTimestamp() : null // ou la date de création de la conv
                    );
                })
                // Optionnel : trier pour avoir les plus récentes en premier
                .sorted((c1, c2) -> c2.lastMessageTimestamp() != null ? c2.lastMessageTimestamp().compareTo(c1.lastMessageTimestamp()) : -1)
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les messages d'une conversation spécifique.
     */
    public List<ChatMessage> findMessagesForConversation(Long conversationId, Long currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));

        if (!conversation.getUser1().getId().equals(currentUserId) && !conversation.getUser2().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Vous n'avez pas le droit de voir cette conversation.");
        }

        return chatMessageRepository.findByConversationOrderByTimestampAsc(conversation);
    }

    /**
     * Démarre une nouvelle conversation ou récupère une existante.
     */
    public Conversation startOrGetConversation(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        User user2 = userRepository.findById(user2Id).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Cherche si une conversation existe déjà
        return conversationRepository.findConversationBetweenUsers(user1.getId(), user2.getId())
                .orElseGet(() -> {
                    // Si non, on la crée
                    Conversation newConversation = new Conversation();
                    newConversation.setUser1(user1);
                    newConversation.setUser2(user2);
                    return conversationRepository.save(newConversation);
                });
    }
}
