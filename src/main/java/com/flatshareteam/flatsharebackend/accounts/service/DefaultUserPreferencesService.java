package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesDto;
import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesRequest;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.model.UserPreferences;
import com.flatshareteam.flatsharebackend.accounts.repository.UserPreferencesRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultUserPreferencesService implements UserPreferencesService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;

    @Override
    @Transactional(readOnly = true)
    public UserPreferencesDto getPreferences(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TenantRole tenantRole = getTenantRole(user);

        return userPreferencesRepository.findByTenantRole_Id(tenantRole.getId())
                .map(UserPreferencesDto::from)
                .orElseGet(() -> new UserPreferencesDto(null, null, null, null, new ArrayList<>()));
    }

    @Override
    @Transactional
    public UserPreferencesDto savePreferences(UUID userId, UserPreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TenantRole tenantRole = getTenantRole(user);

        UserPreferences preferences = userPreferencesRepository.findByTenantRole_Id(tenantRole.getId())
                .orElseGet(UserPreferences::new);

        preferences.setTenantRole(tenantRole);
        applyRequest(preferences, request);

        UserPreferences savedPreferences = userPreferencesRepository.save(preferences);
        return UserPreferencesDto.from(savedPreferences);
    }

    private void applyRequest(UserPreferences preferences, UserPreferencesRequest request) {
        if (request.maxPrice() != null) {
            preferences.setMaxPrice(request.maxPrice());
        }

        if (request.currency() != null) {
            preferences.setCurrency(request.currency());
        }

        if (request.smokingAllowed() != null) {
            preferences.setSmokingAllowed(request.smokingAllowed());
        }

        if (request.petsAllowed() != null) {
            preferences.setPetsAllowed(request.petsAllowed());
        }

        if (request.preferredDistricts() != null) {
            preferences.setPreferredDistricts(new ArrayList<>(request.preferredDistricts()));
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