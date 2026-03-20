package com.flatshareteam.flatsharebackend.accounts.dto;

public record LoginResponse(
        String token,
        String message
) {
}

