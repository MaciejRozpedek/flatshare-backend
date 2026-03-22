package com.flatshareteam.flatsharebackend.security.service;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public String generateToken(String subject) {
        return "placeholder-token-for-" + subject;
    }

    public boolean isTokenValid(String token) {
        return token != null && !token.isBlank();
    }
}

