package com.flatshareteam.flatsharebackend.accounts.controller;

import com.flatshareteam.flatsharebackend.accounts.dto.LoginRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.LoginResponse;
import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationResponse;
import com.flatshareteam.flatsharebackend.accounts.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping()
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

