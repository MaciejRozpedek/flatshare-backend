package com.flatshareteam.flatsharebackend.accounts.dto;

import java.util.UUID;

public record UserDto(
        UUID id,
        String firstName,
        String lastName,
        String email
) {
}
