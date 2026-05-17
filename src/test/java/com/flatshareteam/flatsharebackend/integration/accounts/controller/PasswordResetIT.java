package com.flatshareteam.flatsharebackend.integration.accounts.controller;

import com.flatshareteam.flatsharebackend.accounts.model.AccountStatus;
import com.flatshareteam.flatsharebackend.accounts.model.PasswordResetToken;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.model.UserSession;
import com.flatshareteam.flatsharebackend.accounts.repository.PasswordResetTokenRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import com.flatshareteam.flatsharebackend.accounts.repository.UserSessionRepository;
import com.flatshareteam.flatsharebackend.accounts.service.DefaultPasswordResetService;
import com.flatshareteam.flatsharebackend.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.flatshareteam.flatsharebackend.notifications.port.INotificationPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetIT extends BaseIntegrationTest {

    @Autowired
    private DefaultPasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @MockitoBean
    private INotificationPort notificationPort;

    @Test
    void shouldInvalidateAllSessionsOnPasswordReset() {
        // given
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.reset@example.com");
        user.setPasswordHash("oldPassword");
        user.setStatus(AccountStatus.ACTIVE);
        user = userRepository.save(user);

        UserSession session1 = new UserSession();
        session1.setUser(user);
        session1.setExpiresAt(LocalDateTime.now().plusHours(1));
        userSessionRepository.save(session1);

        UserSession session2 = new UserSession();
        session2.setUser(user);
        session2.setExpiresAt(LocalDateTime.now().plusHours(2));
        userSessionRepository.save(session2);

        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(rawToken);
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);

        assertThat(userSessionRepository.findAll()).hasSize(2);

        // when
        passwordResetService.confirmPasswordReset(rawToken, "NoweBezpieczneHaslo123!");

        // then
        assertThat(userSessionRepository.findAll()).isEmpty();

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getPasswordHash()).isNotEqualTo("oldPassword");
    }
}