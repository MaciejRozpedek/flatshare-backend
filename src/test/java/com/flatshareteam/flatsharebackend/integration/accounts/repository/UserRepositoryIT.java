package com.flatshareteam.flatsharebackend.integration.accounts.repository;

import com.flatshareteam.flatsharebackend.accounts.model.*;
import com.flatshareteam.flatsharebackend.accounts.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        // given
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setEmail("john.smith@test.com");
        user.setPasswordHash("hashed_password");
        user.setStatus(AccountStatus.ACTIVE);

        // when
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findByEmail("john.smith@test.com");

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john.smith@test.com");
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@email.com");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldSaveUserWithRoles() {
        // given
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.doe@test.com");
        user.setPasswordHash("secret");
        user.setStatus(AccountStatus.ACTIVE);

        UserRole userRole = new TenantRole();
        userRole.setRoleType(RoleType.TENANT);
        user.addRole(userRole);

        // when
        User saved = userRepository.save(user);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRoles()).hasSize(1);
        assertThat(saved.getRoles().iterator().next().getRoleType()).isEqualTo(RoleType.TENANT);
    }

    @Test
    void shouldFindAllUsers() {
        // given
        int initialCount = userRepository.findAll().size();

        User user1 = createTestUser("test1@test.com");
        User user2 = createTestUser("test2@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        // when
        var allUsers = userRepository.findAll();

        // then
        assertThat(allUsers).hasSize(initialCount + 2);
    }

    // Helper method
    private User createTestUser(String email) {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(email);
        user.setPasswordHash("pass");
        user.setStatus(AccountStatus.ACTIVE);
        return user;
    }
}