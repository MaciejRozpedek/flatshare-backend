package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesDto;
import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesRequest;
import com.flatshareteam.flatsharebackend.accounts.model.LandlordRole;
import com.flatshareteam.flatsharebackend.accounts.model.RoleType;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.model.UserPreferences;
import com.flatshareteam.flatsharebackend.accounts.repository.UserPreferencesRepository;
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
    private UserPreferencesRepository userPreferencesRepository;

    @InjectMocks
    private DefaultUserPreferencesService userPreferencesService;

    @Test
    void savePreferences_ShouldCreatePreferences_WhenUserExistsAndPreferencesDoNotExist() {
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

        UserPreferences savedPreferences = UserPreferences.builder()
                .id(UUID.randomUUID())
            .tenantRole(tenantRole)
            .maxPrice(request.maxPrice())
                .currency(request.currency())
            .smokingAllowed(request.smokingAllowed())
            .petsAllowed(request.petsAllowed())
            .preferredDistricts(List.copyOf(request.preferredDistricts()))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userPreferencesRepository.findByTenantRole_Id(tenantRole.getId())).thenReturn(Optional.empty());
        when(userPreferencesRepository.save(any(UserPreferences.class))).thenReturn(savedPreferences);

        UserPreferencesDto response = userPreferencesService.savePreferences(userId, request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(1500), response.maxPrice());
        assertEquals("PLN", response.currency());
        verify(userRepository).findById(userId);
        verify(userPreferencesRepository).save(any(UserPreferences.class));
    }

    @Test
    void getPreferences_ShouldThrowNotFound_WhenPreferencesDoNotExist() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setStatus(com.flatshareteam.flatsharebackend.accounts.model.AccountStatus.ACTIVE);

        TenantRole tenantRole = new TenantRole();
        tenantRole.setId(UUID.randomUUID());
        user.addRole(tenantRole);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userPreferencesRepository.findByTenantRole_Id(tenantRole.getId())).thenReturn(Optional.empty());

        UserPreferencesDto response = userPreferencesService.getPreferences(userId);

        assertNotNull(response);
        assertEquals(List.of(), response.preferredDistricts());
        verify(userPreferencesRepository).findByTenantRole_Id(tenantRole.getId());
    }

    @Test
        void savePreferences_ShouldUpdateExistingPreferences_WhenPreferencesExist() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setStatus(com.flatshareteam.flatsharebackend.accounts.model.AccountStatus.ACTIVE);

        TenantRole tenantRole = new TenantRole();
        tenantRole.setId(UUID.randomUUID());
        user.addRole(tenantRole);

        UserPreferences existing = UserPreferences.builder()
            .id(UUID.randomUUID())
            .tenantRole(tenantRole)
            .maxPrice(BigDecimal.valueOf(1200))
            .currency("PLN")
            .smokingAllowed(false)
            .petsAllowed(false)
            .preferredDistricts(List.of("Śródmieście"))
            .build();

        UserPreferencesRequest request = new UserPreferencesRequest(
            BigDecimal.valueOf(1800),
            "PLN",
            false,
            true,
            List.of("Mokotów", "Ochota")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userPreferencesRepository.findByTenantRole_Id(tenantRole.getId())).thenReturn(Optional.of(existing));
        when(userPreferencesRepository.save(any(UserPreferences.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferencesDto response = userPreferencesService.savePreferences(userId, request);

        assertEquals(BigDecimal.valueOf(1800), response.maxPrice());
        assertEquals(true, response.petsAllowed());
        verify(userPreferencesRepository).save(any(UserPreferences.class));
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
        verify(userPreferencesRepository, never()).save(any(UserPreferences.class));
    }
}