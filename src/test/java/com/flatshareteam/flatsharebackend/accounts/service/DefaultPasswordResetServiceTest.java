package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.model.PasswordResetToken;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.PasswordResetTokenRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import com.flatshareteam.flatsharebackend.common.exception.InvalidResetTokenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.flatshareteam.flatsharebackend.notifications.port.INotificationPort;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private INotificationPort notificationPort;

    @InjectMocks
    private DefaultPasswordResetService passwordResetService;

    @Test
    void requestPasswordReset_ShouldCleanupAndCreateToken_WhenUserExists() {
        String email = "john.doe@example.com";

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        passwordResetService.requestPasswordReset(email);

        verify(tokenRepository).deleteByExpiresAtBeforeOrUsedTrue(any(LocalDateTime.class));
        verify(tokenRepository).deleteByUser_Id(user.getId());
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void requestPasswordReset_ShouldNotCreateToken_WhenUserDoesNotExist() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset(email);

        verify(tokenRepository).deleteByExpiresAtBeforeOrUsedTrue(any(LocalDateTime.class));
        verify(tokenRepository, never()).deleteByUser_Id(any(UUID.class));
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void confirmPasswordReset_ShouldChangePasswordAndDeleteTokens_WhenTokenIsValid() {
        String token = "valid-token";
        String newPassword = "newPassword123";

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("john.doe@example.com");

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setUsed(false);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded-password");

        passwordResetService.confirmPasswordReset(token, newPassword);

        assertEquals("encoded-password", user.getPasswordHash());
        verify(userRepository).save(user);
        verify(tokenRepository).deleteByUser_Id(user.getId());
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    void confirmPasswordReset_ShouldDeleteExpiredTokenAndThrow_WhenTokenIsExpired() {
        String token = "expired-token";

        User user = new User();
        user.setId(UUID.randomUUID());

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setUsed(false);
        resetToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        assertThrows(InvalidResetTokenException.class,
                () -> passwordResetService.confirmPasswordReset(token, "newPassword123"));

        verify(tokenRepository).delete(resetToken);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void cleanupExpiredAndUsedTokens_ShouldDeleteExpiredAndUsedRows() {
        passwordResetService.cleanupExpiredAndUsedTokens();

        verify(tokenRepository).deleteByExpiresAtBeforeOrUsedTrue(any(LocalDateTime.class));
    }
}
