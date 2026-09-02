package com.raya.activitytracking.usermanagement.service;

import com.raya.activitytracking.usermanagement.dto.request.UserRequest;
import com.raya.activitytracking.usermanagement.dto.response.UserResponse;
import com.raya.activitytracking.usermanagement.entity.Role;
import com.raya.activitytracking.usermanagement.entity.User;
import com.raya.activitytracking.usermanagement.repository.RoleRepository;
import com.raya.activitytracking.usermanagement.repository.UserRepository;
import com.raya.activitytracking.usermanagement.service.impl.UserServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("addNewUser: saves a new user with an encoded password when username is free")
    void addNewUser_shouldCreateUser_whenUsernameIsAvailable() {
        UserRequest request = new UserRequest(
                "jdoe", "plainPassword", "jdoe@example.com", null, null, 1L
        );
        Role role = new Role();
        role.setId(1L);
        role.setName("Employee");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUserName("jdoe");
        savedUser.setRole(role);

        when(userRepository.existsByUserName("jdoe")).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.addNewUser(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("addNewUser: throws when the username is already taken")
    void addNewUser_shouldThrow_whenUsernameIsTaken() {
        UserRequest request = new UserRequest(
                "jdoe", "plainPassword", "jdoe@example.com", null, null, 1L
        );
        when(userRepository.existsByUserName("jdoe")).thenReturn(true);

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
        when(userRepository.existsByUserName("jdoe")).thenReturn(false);
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

        UserResponse result = userService.getUserByUserName("jdoe");

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

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUserName("jdoe");
        savedUser.setRole(role);

        when(userRepository.existsByUserName("jdoe")).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.addNewUser(request);

        verify(passwordEncoder).encode("plainPassword");
    }

    @Test
    @DisplayName("setActiveStatus: deactivates the user when found")
    void setActiveStatus_shouldDeactivateUser_whenUserExists() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        userService.setActiveStatus(1L, false);

        assertThat(existingUser.isActive()).isFalse();
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("setActiveStatus: reactivates the user when found")
    void setActiveStatus_shouldReactivateUser_whenUserExists() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        userService.setActiveStatus(1L, true);

        assertThat(existingUser.isActive()).isTrue();
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("setActiveStatus: throws when the user does not exist")
    void setActiveStatus_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.setActiveStatus(99L, false))
                .isInstanceOf(RuntimeException.class);

        verify(userRepository, never()).save(any());
    }
}