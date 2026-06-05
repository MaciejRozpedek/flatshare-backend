package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationResponse;

import java.util.UUID;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    void confirmPasswordReset(String token,String newPassword);
}
