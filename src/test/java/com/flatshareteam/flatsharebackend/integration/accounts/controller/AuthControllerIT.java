package com.flatshareteam.flatsharebackend.integration.accounts.controller;

import com.flatshareteam.flatsharebackend.accounts.dto.LoginRequest;
import com.flatshareteam.flatsharebackend.accounts.model.AccountStatus;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.model.UserSession;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserSessionRepository;
import com.flatshareteam.flatsharebackend.integration.BaseIntegrationTest;
import com.flatshareteam.flatsharebackend.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIT extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private final String plainPassword = "SecretPassword123!";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPasswordHash(passwordEncoder.encode(plainPassword));
        testUser.setStatus(AccountStatus.ACTIVE);

        TenantRole role = new TenantRole();
        testUser.addRole(role);

        userRepository.save(testUser);
    }

    // ====================== LOGIN TESTS ======================

    @Test
    void shouldLoginSuccessfullyAndReturnToken() throws Exception {
        // given
        LoginRequest request = new LoginRequest(testUser.getEmail(), plainPassword);

        // when & then
        mockMvc.perform(post("/api/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.roles[0]").value("TENANT"));
    }

    @Test
    void shouldFailLoginWithIncorrectPassword() throws Exception {
        // given
        LoginRequest request = new LoginRequest(testUser.getEmail(), "WrongPassword!");

        // when & then
        mockMvc.perform(post("/api/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailLoginWithNonExistingEmail() throws Exception {
        // given
        LoginRequest request = new LoginRequest("non.existing@example.com", plainPassword);

        // when & then
        mockMvc.perform(post("/api/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailLoginWithInvalidEmailFormat() throws Exception {
        // given
        LoginRequest request = new LoginRequest("invalid-email", plainPassword);

        // when & then
        mockMvc.perform(post("/api/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Invalid email format")));
    }

    // ====================== REFRESH SESSION TESTS ======================

    @Test
    void shouldRefreshSessionSuccessfullyAndReturnNewToken() throws Exception {
        // given
        UserSession session = new UserSession();
        session.setUser(testUser);
        session.setExpiresAt(LocalDateTime.now().plusHours(1));
        userSessionRepository.save(session);

        String oldToken = jwtService.generateToken(testUser, session.getId());

        // when & then
        mockMvc.perform(patch("/api/v1/sessions/{sessionId}", session.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + oldToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.token", not(oldToken)))
                .andExpect(jsonPath("$.sessionId").value(session.getId().toString()))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.roles[0]").value("TENANT"));
    }

    @Test
    void shouldFailRefreshWhenSessionIsExpired() throws Exception {
        // given
        UserSession session = new UserSession();
        session.setUser(testUser);
        session.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        userSessionRepository.save(session);

        String token = jwtService.generateToken(testUser, session.getId());

        // when & then
        mockMvc.perform(patch("/api/v1/sessions/{sessionId}", session.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Session expired"));
    }

    @Test
    void shouldFailRefreshWhenSessionDoesNotExist() throws Exception {
        // given
        UUID nonExistingSessionId = UUID.randomUUID();
        String token = jwtService.generateToken(testUser, nonExistingSessionId);

        // when & then
        mockMvc.perform(patch("/api/v1/sessions/{sessionId}", nonExistingSessionId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailRefreshWhenNoAuthTokenProvided() throws Exception {
        // given
        UUID sessionId = UUID.randomUUID();

        // when & then
        mockMvc.perform(patch("/api/v1/sessions/{sessionId}", sessionId))
                .andExpect(status().isUnauthorized());
    }
}