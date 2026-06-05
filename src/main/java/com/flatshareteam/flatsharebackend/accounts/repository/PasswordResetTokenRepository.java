package com.flatshareteam.flatsharebackend.accounts.repository;

import com.flatshareteam.flatsharebackend.accounts.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);

    long deleteByExpiresAtBeforeOrUsedTrue(LocalDateTime expiresAt);

    long deleteByUser_Id(UUID userId);
}