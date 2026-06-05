package com.flatshareteam.flatsharebackend.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BanUserRequest(
        @NotBlank
        @Size(max = 500)
        String reason
) {
}
