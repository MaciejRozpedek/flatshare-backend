package com.flatshareteam.flatsharebackend.accounts.dto;

public record PasswordResetConfirmRequest (
        String resetToken,
        String newPassword
)
{

}