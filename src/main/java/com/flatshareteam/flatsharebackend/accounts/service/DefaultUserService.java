package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationResponse;
import com.flatshareteam.flatsharebackend.accounts.model.Role;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultUserService implements UserService {

    @Override
    public RegistrationResponse createUser(RegistrationRequest request) {
        return null;
    }

    @Override
    public void deleteAccount(UUID userId) {

    }

    @Override
    public void assignRole(UUID userId, Role role) {

    }
}
