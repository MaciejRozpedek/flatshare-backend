package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.RegistrationResponse;
import com.flatshareteam.flatsharebackend.accounts.model.AccountStatus;
import com.flatshareteam.flatsharebackend.accounts.model.RoleType;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DefaultUserService defaultUserService;

    @Test
    void createUser_ShouldReturnRegistrationResponse_WhenEmailIsUnique() {
        RegistrationRequest request = new RegistrationRequest("John", "Doe", "john.doe@example.com", "password123", RoleType.TENANT);
        
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setFirstName(request.firstName());
        savedUser.setLastName(request.lastName());
        savedUser.setEmail(request.email());
        savedUser.setStatus(AccountStatus.ACTIVE);
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegistrationResponse response = defaultUserService.createUser(request);

        assertNotNull(response);
        assertEquals("New user created", response.message());
        assertNotNull(response.user());
        assertEquals(request.email(), response.user().email());
        
        verify(userRepository, times(1)).findByEmail(request.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        RegistrationRequest request = new RegistrationRequest("John", "Doe", "john.doe@example.com", "password123", RoleType.TENANT);
        
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> defaultUserService.createUser(request));
            
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Email already exists", exception.getReason());
        
        verify(userRepository, times(1)).findByEmail(request.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteAccount_ShouldThrowUnsupportedOperationException() {
        UUID userId = UUID.randomUUID();
        
        assertThrows(UnsupportedOperationException.class, () -> defaultUserService.deleteAccount(userId));
    }
}
