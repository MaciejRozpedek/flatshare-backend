package com.flatshareteam.flatsharebackend.accounts.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String token,
        UUID sessionId,
        String type,
        long expiresIn,
        List<String> roles
) {
}

