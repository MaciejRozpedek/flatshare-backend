package com.flatshareteam.flatsharebackend.accounts.repository;

import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRoleRepository extends JpaRepository<TenantRole, UUID> {
    Optional<TenantRole> findByUserId(UUID userId);
}
