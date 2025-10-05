package com.homework.backend.repositories;

import com.homework.backend.models.Conversation;
import com.homework.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    // Trouve toutes les conversations où l'utilisateur est soit user1, soit user2
    List<Conversation> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);

    // Trouve une conversation existante entre deux utilisateurs, peu importe l'ordre
    @Query("SELECT c FROM Conversation c WHERE (c.user1.id = :userId1 AND c.user2.id = :userId2) OR (c.user1.id = :userId2 AND c.user2.id = :userId1)")
    Optional<Conversation> findConversationBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
