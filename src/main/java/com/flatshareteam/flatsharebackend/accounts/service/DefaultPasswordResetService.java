package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.model.PasswordResetToken;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.PasswordResetTokenRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import com.flatshareteam.flatsharebackend.common.exception.InvalidResetTokenException;
import com.flatshareteam.flatsharebackend.common.exception.WeakPasswordException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.flatshareteam.flatsharebackend.notifications.port.INotificationPort;
import com.flatshareteam.flatsharebackend.notifications.model.NotificationData;
import com.flatshareteam.flatsharebackend.notifications.model.NotificationType;

@Service
@RequiredArgsConstructor
public class DefaultPasswordResetService implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final INotificationPort notificationPort;

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteByExpiresAtBeforeOrUsedTrue(now);

        userRepository.findByEmail(email).ifPresent(user -> {
            tokenRepository.deleteByUser_Id(user.getId());

            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setCreatedAt(now);
            resetToken.setExpiresAt(now.plusMinutes(15));
            resetToken.setUsed(false);

            tokenRepository.save(resetToken);

            NotificationData notificationMessage = new NotificationData(
                    NotificationType.PASSWORD_RESET,
                    Map.of(
                            "email", email,
                            "resetToken", token
                    )
            );
            notificationPort.notify(user.getId(), notificationMessage);
            
            System.out.println("DEBUG: Wygenerowano token dla " + email + ": " + token);
        });
    }


    @Override
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidResetTokenException("Podany token nie istnieje"));

        if (resetToken.isUsed()) {
            throw new InvalidResetTokenException("Ten token został już wykorzystany");
        }

        if (resetToken.getExpiresAt().isBefore(now)) {
            tokenRepository.delete(resetToken);
            throw new InvalidResetTokenException("Ten token już wygasł");
        }

        if (newPassword.length() < 8) {
            throw new WeakPasswordException("Hasło musi mieć co najmniej 8 znaków");
        }


        User user = resetToken.getUser();

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
        tokenRepository.deleteByUser_Id(user.getId());

        System.out.println("DEBUG: Hasło dla użytkownika " + user.getEmail() + " zostało zmienione.");
    }

    @Scheduled(
            fixedDelayString = "${security.password-reset.cleanup-interval-ms:3600000}",
            initialDelayString = "${security.password-reset.cleanup-initial-delay-ms:300000}"
    )
    @Transactional
    public void cleanupExpiredAndUsedTokens() {
        tokenRepository.deleteByExpiresAtBeforeOrUsedTrue(LocalDateTime.now());
    }
}