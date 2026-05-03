package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.LoginRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.LoginResponse;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.model.UserSession;
import com.flatshareteam.flatsharebackend.accounts.repository.UserSessionRepository;
import com.flatshareteam.flatsharebackend.security.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserSessionRepository userSessionRepository;
    private final long expirationTimeMs;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService, UserSessionRepository userSessionRepository,
                       @Value("${jwt.expiration}") long expirationTimeMs) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userSessionRepository = userSessionRepository;
        this.expirationTimeMs = expirationTimeMs;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = (User) authentication.getPrincipal();
        LocalDateTime expiresAt = java.time.LocalDateTime.now().plusSeconds(expirationTimeMs / 1000);
        UserSession session = new UserSession();
        session.setUser(user);
        session.setExpiresAt(expiresAt);
        session = userSessionRepository.save(session);

        String token = jwtService.generateToken(user, session.getId());

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toList());

        long expiresIn = expirationTimeMs / 1000;

        return new LoginResponse(
                token,
                session.getId(),
                "Bearer",
                expiresIn,
                roles
        );
    }

    public LoginResponse refreshSession(UUID sessionId, User user) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Session not found"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Session expired");
        }

        session.setExpiresAt(LocalDateTime.now().plusSeconds(expirationTimeMs / 1000));
        userSessionRepository.save(session);

        String token = jwtService.generateToken(user, sessionId);
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toList());

        long expiresIn = expirationTimeMs / 1000;

        return new LoginResponse(
                token,
                sessionId,
                "Bearer",
                expiresIn,
                roles
        );
    }
}

