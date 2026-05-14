package com.flatshareteam.flatsharebackend.integration.accounts.repository;

import com.flatshareteam.flatsharebackend.accounts.model.AccountStatus;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.model.UserPreferences;
import com.flatshareteam.flatsharebackend.accounts.repository.UserPreferencesRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserPreferencesRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Test
    void shouldPersistAndFindPreferencesByUserId() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setEmail("john.preferences@test.com");
        user.setPasswordHash("hashed_password");
        user.setStatus(AccountStatus.ACTIVE);

        TenantRole tenantRole = new TenantRole();
        user.addRole(tenantRole);

        User savedUser = userRepository.save(user);

        UserPreferences preferences = UserPreferences.builder()
            .tenantRole(savedUser.getRoles().stream().map(role -> (TenantRole) role).findFirst().orElseThrow())
            .maxPrice(BigDecimal.valueOf(2500))
                .currency("PLN")
            .smokingAllowed(false)
            .petsAllowed(true)
            .preferredDistricts(List.of("Mokotów", "Ochota"))
                .build();

        UserPreferences savedPreferences = userPreferencesRepository.save(preferences);

        assertThat(savedPreferences.getId()).isNotNull();
        assertThat(userPreferencesRepository.findByTenantRole_Id(savedUser.getRoles().stream().map(role -> role.getId()).findFirst().orElseThrow())).isPresent();
        assertThat(userPreferencesRepository.findByTenantRole_Id(savedUser.getRoles().stream().map(role -> role.getId()).findFirst().orElseThrow()).orElseThrow().getCurrency()).isEqualTo("PLN");
    }
}