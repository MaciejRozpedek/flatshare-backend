package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.LoginRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.LoginResponse;
import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public RegistrationResponse register(RegistrationRequest request) {
        return new RegistrationResponse("placeholder-user-id", "Registration placeholder successful");
    }

    public LoginResponse login(LoginRequest request) {
        return new LoginResponse("placeholder-jwt-token", "Login placeholder successful");
    }
}

