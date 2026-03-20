package com.flatshareteam.flatsharebackend.accounts.dto;

public record LoginRequest(
        String email,
        String password
) {
}

