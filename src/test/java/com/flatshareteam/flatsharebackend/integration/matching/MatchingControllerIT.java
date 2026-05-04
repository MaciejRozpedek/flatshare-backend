package com.flatshareteam.flatsharebackend.integration.matching;

import com.flatshareteam.flatsharebackend.accounts.model.AccountStatus;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import com.flatshareteam.flatsharebackend.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MatchingControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String tenantToken;
    private String landlordToken;

    @BeforeEach
    void setUp() {
        tenantToken = "Bearer " + jwtService.generateToken(createTenantWithPreferences());
        landlordToken = "Bearer " + jwtService.generateToken(createLandlord());
    }

    @Test
    void getMatches_ShouldReturn200_WhenTenantHasPreferences() throws Exception {
        mockMvc.perform(get("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, tenantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getMatches_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/matches"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMatches_ShouldReturn403_WhenUserIsNotTenant() throws Exception {
        mockMvc.perform(get("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, landlordToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMatches_ShouldReturn200WithEmptyList_WhenTenantHasNoPreferencesSet() throws Exception {
        User tenantWithoutPrefs = new User();
        tenantWithoutPrefs.setFirstName("No");
        tenantWithoutPrefs.setLastName("Prefs");
        tenantWithoutPrefs.setEmail("noprefs@test.com");
        tenantWithoutPrefs.setPasswordHash("hash");
        tenantWithoutPrefs.setStatus(AccountStatus.ACTIVE);

        TenantRole role = new TenantRole();
        tenantWithoutPrefs.addRole(role);
        userRepository.save(tenantWithoutPrefs);

        String token = "Bearer " + jwtService.generateToken(tenantWithoutPrefs);

        mockMvc.perform(get("/api/v1/matches")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    private User createTenantWithPreferences() {
        User user = new User();
        user.setFirstName("Jan");
        user.setLastName("Lokator");
        user.setEmail("tenant@test.com");
        user.setPasswordHash("hash");
        user.setStatus(AccountStatus.ACTIVE);

        TenantRole role = new TenantRole();
        role.setMaxPrice(new BigDecimal("2000"));
        role.setCurrency("PLN");
        role.setPetsAllowed(true);
        role.setSmokingAllowed(false);

        user.addRole(role);
        return userRepository.save(user);
    }

    private User createLandlord() {
        User user = new User();
        user.setFirstName("Joe");
        user.setLastName("Charles");
        user.setEmail("landlord@test.com");
        user.setPasswordHash("hash");
        user.setStatus(AccountStatus.ACTIVE);

        com.flatshareteam.flatsharebackend.accounts.model.LandlordRole role =
                new com.flatshareteam.flatsharebackend.accounts.model.LandlordRole();
        user.addRole(role);
        return userRepository.save(user);
    }
}