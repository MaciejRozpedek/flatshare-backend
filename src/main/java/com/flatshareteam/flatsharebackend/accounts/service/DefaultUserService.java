package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationResponse;
import com.flatshareteam.flatsharebackend.accounts.dto.UserDto;
import com.flatshareteam.flatsharebackend.accounts.model.*;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegistrationResponse createUser(RegistrationRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        UserRole userRole = switch (request.role()) {
            case TENANT -> new TenantRole();
            case LANDLORD -> new LandlordRole();
            case ADMIN -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign ADMIN role during registration");
        };

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(AccountStatus.ACTIVE);
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        return new RegistrationResponse(
                "New user created",
                new UserDto(
                        savedUser.getId(),
                        savedUser.getFirstName(),
                        savedUser.getLastName(),
                        savedUser.getEmail(),
                        userRole.getRoleType()
                )
        );
    }

    @Override
    public void deleteAccount(UUID userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // TO DO
//    @Override
//    public void assignRole(UUID userId, Role role) {}
}
