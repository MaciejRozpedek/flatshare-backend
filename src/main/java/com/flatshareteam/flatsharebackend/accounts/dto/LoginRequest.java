package com.flatshareteam.flatsharebackend.accounts.dto;

public record LoginRequest(
        String login,
        String password
) {
}

