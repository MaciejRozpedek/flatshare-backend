package com.flatshareteam.flatsharebackend.accounts.dto;

public record RegistrationResponse(
        String message,
        UserDto user
) {
}
