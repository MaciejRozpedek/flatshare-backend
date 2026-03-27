package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.model.PasswordResetToken;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.repository.PasswordResetTokenRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import com.flatshareteam.flatsharebackend.common.exception.InvalidResetTokenException;
import com.flatshareteam.flatsharebackend.common.exception.WeakPasswordException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DefaultPasswordResetService implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setCreatedAt(LocalDateTime.now());
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            resetToken.setUsed(false);

            tokenRepository.save(resetToken);

            // TODO: wysylka emaila
            System.out.println("DEBUG: Wygenerowano token dla " + email + ": " + token);
        });
    }


    @Override
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidResetTokenException("Podany token nie istnieje"));

        if (resetToken.isUsed()) {
            throw new InvalidResetTokenException("Ten token został już wykorzystany");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidResetTokenException("Ten token już wygasł");
        }

        if (newPassword.length() < 8) {
            throw new WeakPasswordException("Hasło musi mieć co najmniej 8 znaków");
        }


        User user = resetToken.getUser();

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);
        userRepository.save(user);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        System.out.println("DEBUG: Hasło dla użytkownika " + user.getEmail() + " zostało zmienione.");
    }
}