package com.flatshareteam.flatsharebackend.accounts.repository;

import com.flatshareteam.flatsharebackend.accounts.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    void deleteAllByUser_Id(UUID userId);
    void deleteByExpiresAtBefore(LocalDateTime now);
}