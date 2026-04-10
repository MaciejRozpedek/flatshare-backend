package com.flatshareteam.flatsharebackend.accounts.dto;

import com.flatshareteam.flatsharebackend.accounts.model.RoleType;

import java.util.UUID;

public record UserDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        RoleType role
) {
}
