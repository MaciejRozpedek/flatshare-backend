package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationResponse;
import com.flatshareteam.flatsharebackend.accounts.model.Role;

import java.util.UUID;

public interface UserService {
    RegistrationResponse createUser(RegistrationRequest request);
    void deleteAccount(UUID userId);
    void assignRole(UUID userId, Role role);
}
