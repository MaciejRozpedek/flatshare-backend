package com.flatshareteam.flatsharebackend.accounts.repository;

import com.flatshareteam.flatsharebackend.accounts.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {

    Optional<UserPreferences> findByTenantRole_Id(UUID tenantRoleId);

    boolean existsByTenantRole_Id(UUID tenantRoleId);
}