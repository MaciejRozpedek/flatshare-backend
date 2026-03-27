package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.LoginRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.LoginResponse;
import com.flatshareteam.flatsharebackend.accounts.model.TenantRole;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

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

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // setting JWT expiration to 3600000ms (1 hour)
        authService = new AuthService(authenticationManager, jwtService, 3600000L);
    }

    @Test
    void login_ShouldReturnLoginResponse() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User mockUser = new User();
        mockUser.setEmail(request.email());
        mockUser.setPasswordHash("encodedPass");
        TenantRole role = new TenantRole();
        mockUser.addRole(role);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(mockUser)).thenReturn("mockJwtToken");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.token());
        assertEquals("Bearer", response.type());
        assertEquals(3600L, response.expiresIn());
        assertFalse(response.roles().isEmpty());
        assertEquals("TENANT", response.roles().getFirst());
    }

    @Test
    void refreshSession_ShouldReturnNewToken() {
        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        TenantRole role = new TenantRole();
        mockUser.addRole(role);

        UUID sessionId = UUID.randomUUID();

        when(jwtService.generateToken(mockUser)).thenReturn("newMockJwtToken");

        LoginResponse response = authService.refreshSession(sessionId, mockUser);

        assertNotNull(response);
        assertEquals("newMockJwtToken", response.token());
        assertEquals(sessionId, response.sessionId());
        assertEquals("Bearer", response.type());
        assertEquals(3600L, response.expiresIn());
        assertFalse(response.roles().isEmpty());
        assertEquals("TENANT", response.roles().getFirst());
    }
}
