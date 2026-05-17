package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesDto;
import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesRequest;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.TenantRoleRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultUserPreferencesService implements UserPreferencesService {

    private final UserRepository userRepository;
    private final TenantRoleRepository tenantRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserPreferencesDto getPreferences(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TenantRole tenantRole = getTenantRole(user);

        return UserPreferencesDto.from(tenantRole);
    }

    @Override
    @Transactional
    public UserPreferencesDto savePreferences(UUID userId, UserPreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TenantRole tenantRole = getTenantRole(user);

        applyRequest(tenantRole, request);

        TenantRole savedRole = tenantRoleRepository.save(tenantRole);
        return UserPreferencesDto.from(savedRole);
    }

    private void applyRequest(TenantRole tenantRole, UserPreferencesRequest request) {
        if (request.maxPrice() != null) {
            tenantRole.setMaxPrice(request.maxPrice());
        }

        if (request.currency() != null) {
            tenantRole.setCurrency(request.currency());
        }

        if (request.smokingAllowed() != null) {
            tenantRole.setSmokingAllowed(request.smokingAllowed());
        }

        if (request.petsAllowed() != null) {
            tenantRole.setPetsAllowed(request.petsAllowed());
        }

        if (request.preferredDistricts() != null) {
            tenantRole.setPreferredDistricts(new HashSet<>(request.preferredDistricts()));
        }
    }

    private TenantRole getTenantRole(User user) {
        return user.getRoles().stream()
                .filter(TenantRole.class::isInstance)
                .map(TenantRole.class::cast)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant preferences are available only for tenant accounts"));
    }
}