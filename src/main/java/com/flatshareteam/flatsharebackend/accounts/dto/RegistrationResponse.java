package com.flatshareteam.flatsharebackend.accounts.dto;

import com.flatshareteam.flatsharebackend.accounts.dto.UserDto;

public record RegistrationResponse(
        String message,
        UserDto user
) {
}
