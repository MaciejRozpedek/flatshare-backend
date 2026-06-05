package com.flatshareteam.flatsharebackend.accounts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequestRequest (
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email should be valid")
        String email
)
{

}

