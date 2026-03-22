package com.flatshareteam.flatsharebackend.accounts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegistrationRequest(
        @NotBlank(message = "First name is mandatory")
        String firstName,
        
        @NotBlank(message = "Last name is mandatory")
        String lastName,
        
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email should be valid")
        String email,
        
        @NotBlank(message = "Password is mandatory")
        String password
) {
}
