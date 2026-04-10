package com.flatshareteam.flatsharebackend.accounts.repository;

import com.flatshareteam.flatsharebackend.accounts.model.LandlordRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LandlordRoleRepository extends JpaRepository<LandlordRole, UUID> {
    Optional<LandlordRole> findByUserId(UUID userId);
}
