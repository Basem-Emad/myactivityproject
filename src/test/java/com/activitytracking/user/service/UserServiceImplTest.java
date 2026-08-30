package com.activitytracking.user.service;

import com.activitytracking.user.dto.request.AssignRoleRequestDto;
import com.activitytracking.user.dto.request.CreateUserRequestDto;
import com.activitytracking.user.dto.request.UpdateUserRequestDto;
import com.activitytracking.user.dto.response.UserResponseDto;
import com.activitytracking.user.entity.Role;
import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.UserRepository;
import com.activitytracking.user.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User existingUser;
    private CreateUserRequestDto createRequest;
    private UpdateUserRequestDto updateRequest;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Ahmed Yehia");
        existingUser.setEmail("ahmed@raya.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setRole(Role.EMPLOYEE);
        existingUser.setActive(true);

        createRequest = new CreateUserRequestDto();
        createRequest.setName("Ahmed Yehia");
        createRequest.setEmail("ahmed@raya.com");
        createRequest.setPassword("plainPassword");
        createRequest.setRole(Role.EMPLOYEE);

        updateRequest = new UpdateUserRequestDto();
        updateRequest.setName("Ahmed Yehia Updated");
        updateRequest.setEmail("ahmed.updated@raya.com");
        updateRequest.setActive(true);
    }

    @Test
    void createUser_shouldSaveAndReturnResponse_whenEmailDoesNotExist() {
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(createRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserResponseDto response = userService.createUser(createRequest);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("ahmed@raya.com");
        assertThat(response.getRole()).isEqualTo(Role.EMPLOYEE);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already in use");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void listUsers_shouldReturnListOfResponses() {
        when(userRepository.findAll()).thenReturn(List.of(existingUser));

        List<UserResponseDto> responses = userService.listUsers();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getEmail()).isEqualTo("ahmed@raya.com");
    }

    @Test
    void updateUser_shouldUpdateAndReturnResponse_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserResponseDto response = userService.updateUser(1L, updateRequest);

        assertThat(response.getId()).isEqualTo(1L);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void assignRole_shouldUpdateRole_whenUserExists() {
        AssignRoleRequestDto roleRequest = new AssignRoleRequestDto();
        roleRequest.setRole(Role.MANAGER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        userService.assignRole(1L, roleRequest);

        assertThat(existingUser.getRole()).isEqualTo(Role.MANAGER);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void assignRole_shouldThrowException_whenUserDoesNotExist() {
        AssignRoleRequestDto roleRequest = new AssignRoleRequestDto();
        roleRequest.setRole(Role.MANAGER);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignRole(99L, roleRequest))
                .isInstanceOf(EntityNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}