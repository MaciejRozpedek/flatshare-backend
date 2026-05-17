package com.flatshareteam.flatsharebackend.accounts.controller;

import com.flatshareteam.flatsharebackend.accounts.dto.LoginRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.LoginResponse;
import com.flatshareteam.flatsharebackend.accounts.dto.SessionResponse;
import com.flatshareteam.flatsharebackend.accounts.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import java.net.URI;        
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping()
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.sessionId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{sessionId}")
    public ResponseEntity<LoginResponse> refreshSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal User user) {
        
        LoginResponse response = authService.refreshSession(sessionId, user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.getSession(sessionId, user));
    }
}

