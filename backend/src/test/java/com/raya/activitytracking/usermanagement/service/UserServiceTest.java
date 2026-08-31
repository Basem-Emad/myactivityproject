package com.raya.activitytracking.usermanagement.service;

import com.raya.activitytracking.usermanagement.dto.request.UserRequest;
import com.raya.activitytracking.usermanagement.entity.Role;
import com.raya.activitytracking.usermanagement.entity.User;
import com.raya.activitytracking.usermanagement.repository.RoleRepository;
import com.raya.activitytracking.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("addNewUser: saves a new user with an encoded password when username is free")
    void addNewUser_shouldCreateUser_whenUsernameIsAvailable() {
        UserRequest request = new UserRequest(
                "jdoe", "plainPassword", "jdoe@example.com", null, null, 1L
        );
        Role role = new Role();
        role.setId(1L);
        role.setName("Employee");

        when(userRepository.findByUserName("jdoe")).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        userService.addNewUser(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("addNewUser: throws when the username is already taken")
    void addNewUser_shouldThrow_whenUsernameIsTaken() {
        UserRequest request = new UserRequest(
                "jdoe", "plainPassword", "jdoe@example.com", null, null, 1L
        );
        when(userRepository.findByUserName("jdoe"))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.addNewUser(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UserName is taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("addNewUser: throws when the requested role does not exist")
    void addNewUser_shouldThrow_whenRoleNotFound() {
        UserRequest request = new UserRequest(
                "jdoe", "plainPassword", "jdoe@example.com", null, null, 99L
        );
        when(userRepository.findByUserName("jdoe")).thenReturn(Optional.empty());
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addNewUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role not found");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserById: throws when no user exists with the given id")
    void getUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("getUserByUserName: throws instead of returning null when user is missing")
    void getUserByUserName_shouldThrow_whenNotFound() {
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUserName("ghost"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    @DisplayName("getUserByUserName: returns the user when found")
    void getUserByUserName_shouldReturnUser_whenFound() {
        User user = new User();
        user.setUserName("jdoe");
        when(userRepository.findByUserName("jdoe")).thenReturn(Optional.of(user));

        User result = userService.getUserByUserName("jdoe");

        assertThat(result.getUserName()).isEqualTo("jdoe");
    }

    @Test
    @DisplayName("addNewUser: encodes the raw password before saving")
    void addNewUser_shouldEncodePassword() {
        UserRequest request = new UserRequest(
                "jdoe", "plainPassword", "jdoe@example.com", null, null, 1L
        );
        Role role = new Role();
        role.setId(1L);

        when(userRepository.findByUserName("jdoe")).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        userService.addNewUser(request);

        verify(passwordEncoder).encode("plainPassword");
    }
}

