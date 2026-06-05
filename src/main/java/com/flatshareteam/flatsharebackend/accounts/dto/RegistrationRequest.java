package com.flatshareteam.flatsharebackend.accounts.dto;

import com.flatshareteam.flatsharebackend.accounts.model.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrationRequest(
        @NotBlank(message = "First name is mandatory")
        String firstName,
        
        @NotBlank(message = "Last name is mandatory")
        String lastName,
        
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email should be valid")
        String email,
        
        @NotBlank(message = "Password is mandatory")
        String password,

        @NotNull(message = "Role is mandatory")
        RoleType role
) {
}
