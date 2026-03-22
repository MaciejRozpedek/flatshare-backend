package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.LoginRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.LoginResponse;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.security.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long expirationTimeMs;

    public AuthService(AuthenticationManager authenticationManager, 
                       JwtService jwtService,
                       @Value("${jwt.expiration}") long expirationTimeMs) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.expirationTimeMs = expirationTimeMs;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toList());

        long expiresIn = expirationTimeMs / 1000;

        return new LoginResponse(
                token,
                UUID.randomUUID(),
                "Bearer",
                expiresIn,
                roles
        );
    }
}

