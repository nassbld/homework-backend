package com.homework.backend.repositories;

import com.homework.backend.models.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    
    Optional<EmailVerificationToken> findByToken(String token);
    
    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.expiresAt < ?1")
    void deleteExpiredTokens(LocalDateTime now);
}

