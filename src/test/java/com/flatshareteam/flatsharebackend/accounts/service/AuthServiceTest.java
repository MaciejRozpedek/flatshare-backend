package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.LoginRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.LoginResponse;
import com.flatshareteam.flatsharebackend.accounts.dto.SessionResponse;
import com.flatshareteam.flatsharebackend.accounts.model.*;
import com.flatshareteam.flatsharebackend.accounts.repository.UserSessionRepository;
import com.flatshareteam.flatsharebackend.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserSessionRepository userSessionRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // setting JWT expiration to 3600000ms (1 hour)
        authService = new AuthService(authenticationManager, jwtService, userSessionRepository, 3600000L);
    }

    @Test
    void login_ShouldReturnLoginResponse() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User mockUser = createUserWithRole(request.email(), RoleType.TENANT);
        UserSession savedSession = createSavedSession(mockUser);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(mockUser, savedSession.getId()))
                .thenReturn("mockJwtToken");
        when(userSessionRepository.save(any(UserSession.class)))
                .thenReturn(savedSession);

        // when
        LoginResponse response = authService.login(request);

        // then
        assertNotNull(response);
        assertEquals("mockJwtToken", response.token());
        assertEquals(savedSession.getId(), response.sessionId());
        assertEquals("Bearer", response.type());
        assertEquals(3600L, response.expiresIn());
        assertEquals(1, response.roles().size());
        assertEquals("TENANT", response.roles().getFirst());
    }

    @Test
    void login_ShouldThrowException_WhenAuthenticationFails() {
        // given
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when & then
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refreshSession_ShouldReturnNewToken_WhenSessionIsValid() {
        // given
        User mockUser = createUserWithRole("test@example.com", RoleType.LANDLORD);
        UserSession userSession = createSavedSession(mockUser);
        UUID sessionId = userSession.getId();

        when(jwtService.generateToken(mockUser, sessionId)).thenReturn("newMockJwtToken");
        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(userSession));

        // when
        LoginResponse response = authService.refreshSession(sessionId, mockUser);

        // then
        assertNotNull(response);
        assertEquals("newMockJwtToken", response.token());
        assertEquals(sessionId, response.sessionId());
        assertEquals("Bearer", response.type());
        assertEquals(3600L, response.expiresIn());
        assertFalse(response.roles().isEmpty());
        assertEquals("TENANT", response.roles().getFirst());
    }

    @Test
    void refreshSession_ShouldThrowUnauthorized_WhenSessionNotFound() {
        // given
        UUID sessionId = UUID.randomUUID();
        User mockUser = createUserWithRole("test@example.com", RoleType.TENANT);

        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        // when & then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.refreshSession(sessionId, mockUser));

        // then
        assertEquals(401, exception.getStatusCode().value());
        assert exception.getReason() != null;
        assertTrue(exception.getReason().contains("Session not found"));
    }

    @Test
    void refreshSession_ShouldThrowUnauthorized_WhenSessionIsExpired() {
        // given
        User mockUser = createUserWithRole("test@example.com", RoleType.TENANT);
        UUID sessionId = UUID.randomUUID();

        UserSession expiredSession = new UserSession();
        expiredSession.setId(sessionId);
        expiredSession.setUser(mockUser);
        expiredSession.setExpiresAt(LocalDateTime.now().minusMinutes(10));

        when(userSessionRepository.findById(sessionId)).thenReturn(Optional.of(expiredSession));

        // when & then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.refreshSession(sessionId, mockUser));

        // then
        assertEquals(401, exception.getStatusCode().value());
        assert exception.getReason() != null;
        assertTrue(exception.getReason().contains("Session expired"));
    }

    @Test
    void getSession_ShouldReturnSessionResponse_WhenSessionIsValid() {
        // given
        User mockUser = createUserWithRole("test@example.com", RoleType.TENANT);
        UserSession userSession = createSavedSession(mockUser);

        when(userSessionRepository.findById(userSession.getId())).thenReturn(Optional.of(userSession));

        // when
        SessionResponse response = authService.getSession(userSession.getId(), mockUser);

        // then
        assertEquals(userSession.getId(), response.sessionId());
        assertEquals(mockUser.getId(), response.userId());
    }

    @Test
    void getSession_ShouldThrowForbidden_WhenSessionBelongsToDifferentUser() {
        // given
        User sessionOwner = createUserWithRole("owner@example.com", RoleType.TENANT);
        User currentUser = createUserWithRole("current@example.com", RoleType.TENANT);
        UserSession userSession = createSavedSession(sessionOwner);

        when(userSessionRepository.findById(userSession.getId())).thenReturn(Optional.of(userSession));

        // when & then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.getSession(userSession.getId(), currentUser));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void login_ShouldHandleUserWithMultipleRoles() {
        // given
        LoginRequest request = new LoginRequest("multi@example.com", "password123");

        User mockUser = new User();
        mockUser.setEmail("multi@example.com");
        mockUser.addRole(new TenantRole());
        mockUser.addRole(new LandlordRole());

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        UserSession savedSession = createSavedSession(mockUser);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userSessionRepository.save(any())).thenReturn(savedSession);
        when(jwtService.generateToken(any(), any())).thenReturn("token123");

        // when
        LoginResponse response = authService.login(request);

        // then
        assertEquals(2, response.roles().size());
        assertTrue(response.roles().contains("TENANT"));
        assertTrue(response.roles().contains("LANDLORD"));
    }

    @Test
    void login_ShouldReturnEmptyRoles_WhenUserHasNoRoles() {
        // given
        LoginRequest request = new LoginRequest("noroles@example.com", "password123");

        User mockUser = new User();
        mockUser.setEmail("noroles@example.com");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        UserSession savedSession = createSavedSession(mockUser);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userSessionRepository.save(any())).thenReturn(savedSession);
        when(jwtService.generateToken(any(), any())).thenReturn("token");

        // when
        LoginResponse response = authService.login(request);

        // then
        assertNotNull(response.roles());
        assertTrue(response.roles().isEmpty());
    }

    private User createUserWithRole(String email, RoleType roleType) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("encodedPass");

        TenantRole role;
        if (roleType.equals(RoleType.TENANT)) {
            role = new TenantRole();
        } else if (roleType.equals(RoleType.LANDLORD)) {
            role = new TenantRole();
        } else {
            throw new IllegalArgumentException("Unsupported role type");
        }
        user.addRole(role);

        return user;
    }

    private UserSession createSavedSession(User user) {
        UserSession session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setExpiresAt(LocalDateTime.now().plusHours(1));
        return session;
    }
}
