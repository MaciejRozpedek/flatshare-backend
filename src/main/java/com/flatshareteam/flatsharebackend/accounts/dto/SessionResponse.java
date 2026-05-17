package com.flatshareteam.flatsharebackend.accounts.dto;

import java.util.UUID;

public record SessionResponse(
        UUID sessionId,
        UUID userId
) {
}
