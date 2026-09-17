package com.activitytracking.user.service.impl;

import com.activitytracking.user.dto.request.AssignRoleRequestDto;
import com.activitytracking.user.dto.request.CreateUserRequestDto;
import com.activitytracking.user.dto.request.UpdateUserRequestDto;
import com.activitytracking.user.dto.response.RoleResponseDto;
import com.activitytracking.user.dto.response.UserResponseDto;
import com.activitytracking.user.entity.Permission;
import com.activitytracking.user.entity.Role;
import com.activitytracking.user.entity.User;
import com.activitytracking.user.repository.RoleRepository;
import com.activitytracking.user.repository.UserRepository;
import com.activitytracking.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        return toResponseDto(user);
    }

    @Override
    public UserResponseDto createUser(CreateUserRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }

        Role role = findRoleOrThrow(request.getRoleId());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);

        return toResponseDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto updateUser(Long id, UpdateUserRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setActive(request.isActive());

        return toResponseDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto assignRole(Long id, AssignRoleRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        user.setRole(findRoleOrThrow(request.getRoleId()));

        return toResponseDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        // Soft delete on purpose: a user's historical activity entries stay valid for
        // reporting even after the account is removed. A hard delete would either fail
        // on the activity_entries FK or silently wipe out real reporting history.
        user.setActive(false);
        userRepository.save(user);
    }

    private Role findRoleOrThrow(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
    }

    private UserResponseDto toResponseDto(User user) {
        Role role = user.getRole();
        List<String> permissionNames = role.getPermissions().stream()
                .map(Permission::getName)
                .sorted()
                .toList();

        RoleResponseDto roleDto = new RoleResponseDto(role.getId(), role.getName(), permissionNames);

        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roleDto,
                user.isActive()
        );
    }
}