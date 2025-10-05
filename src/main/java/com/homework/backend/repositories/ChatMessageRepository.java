package com.homework.backend.repositories;

import com.homework.backend.models.ChatMessage;
import com.homework.backend.models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Trouve le dernier message d'une conversation pour l'afficher dans le snippet
    ChatMessage findTopByConversationOrderByTimestampDesc(Conversation conversation);

    // Trouve tous les messages d'une conversation, triés par date pour l'affichage de l'historique
    List<ChatMessage> findByConversationOrderByTimestampAsc(Conversation conversation);
}
