package com.flatshareteam.flatsharebackend.accounts.dto;

public record RegistrationRequest(
        String email,
        String password
) {
}

