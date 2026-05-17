package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesDto;
import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesRequest;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.TenantRoleRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUserPreferencesServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRoleRepository tenantRoleRepository;

    @InjectMocks
    private DefaultUserPreferencesService userPreferencesService;

    @Test
    void savePreferences_ShouldSavePreferences_WhenUserExists() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("john.doe@example.com");
        user.setStatus(com.flatshareteam.flatsharebackend.accounts.model.AccountStatus.ACTIVE);

        TenantRole tenantRole = new TenantRole();
        tenantRole.setId(UUID.randomUUID());
        user.addRole(tenantRole);

        UserPreferencesRequest request = new UserPreferencesRequest(
            BigDecimal.valueOf(1500),
            "PLN",
            true,
            false,
            List.of("Mokotów", "Ochota")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tenantRoleRepository.save(any(TenantRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferencesDto response = userPreferencesService.savePreferences(userId, request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(1500), response.maxPrice());
        assertEquals("PLN", response.currency());
        verify(userRepository).findById(userId);
        verify(tenantRoleRepository).save(any(TenantRole.class));
    }

    @Test
    void getPreferences_ShouldReturnEmpty_WhenPreferencesAreNotSet() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setStatus(com.flatshareteam.flatsharebackend.accounts.model.AccountStatus.ACTIVE);

        TenantRole tenantRole = new TenantRole();
        tenantRole.setId(UUID.randomUUID());
        user.addRole(tenantRole);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserPreferencesDto response = userPreferencesService.getPreferences(userId);

        assertNotNull(response);
        assertEquals(List.of(), response.preferredDistricts());
    }

    @Test
    void savePreferences_ShouldUpdateExistingPreferences_WhenPreferencesExist() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setStatus(com.flatshareteam.flatsharebackend.accounts.model.AccountStatus.ACTIVE);

        TenantRole tenantRole = new TenantRole();
        tenantRole.setId(UUID.randomUUID());
        tenantRole.setMaxPrice(BigDecimal.valueOf(1200));
        tenantRole.setCurrency("PLN");
        tenantRole.setSmokingAllowed(false);
        tenantRole.setPetsAllowed(false);
        tenantRole.setPreferredDistricts(Set.of("Śródmieście"));
        user.addRole(tenantRole);

        UserPreferencesRequest request = new UserPreferencesRequest(
            BigDecimal.valueOf(1800),
            "PLN",
            false,
            true,
            List.of("Mokotów", "Ochota")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tenantRoleRepository.save(any(TenantRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferencesDto response = userPreferencesService.savePreferences(userId, request);

        assertEquals(BigDecimal.valueOf(1800), response.maxPrice());
        assertEquals(true, response.petsAllowed());
        verify(tenantRoleRepository).save(any(TenantRole.class));
    }

    @Test
    void savePreferences_ShouldRejectNonTenantUser_WhenTenantRoleMissing() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setStatus(com.flatshareteam.flatsharebackend.accounts.model.AccountStatus.ACTIVE);

        UserPreferencesRequest request = new UserPreferencesRequest(
            BigDecimal.valueOf(3000),
            "PLN",
            false,
            true,
            List.of("Mokotów")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userPreferencesService.savePreferences(userId, request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(tenantRoleRepository, never()).save(any(TenantRole.class));
    }
}