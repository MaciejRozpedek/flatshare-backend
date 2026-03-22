package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DefaultUserService implements UserService {

    @Override
    public RegistrationResponse createUser(RegistrationRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteAccount(UUID userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // TO DO
//    @Override
//    public void assignRole(UUID userId, Role role) {}
}
