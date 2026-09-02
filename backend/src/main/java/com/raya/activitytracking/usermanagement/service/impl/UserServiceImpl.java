package com.raya.activitytracking.usermanagement.service.impl;

import com.raya.activitytracking.usermanagement.dto.request.RegisterRequest;
import com.raya.activitytracking.usermanagement.dto.request.UserRequest;
import com.raya.activitytracking.usermanagement.dto.response.UserResponse;
import com.raya.activitytracking.usermanagement.entity.Role;
import com.raya.activitytracking.usermanagement.entity.User;
import com.raya.activitytracking.usermanagement.repository.RoleRepository;
import com.raya.activitytracking.usermanagement.repository.UserRepository;
import com.raya.activitytracking.usermanagement.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_SELF_REGISTER_ROLE = "Employee";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUserName(String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with username: " + userName));
        return toResponse(user);
    }

    @Override
    public UserResponse addNewUser(UserRequest request) {
        ensureUserNameAvailable(request.getUserName());

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        User user = buildUser(request.getUserName(), request.getPassword(),
                request.getEmail(), request.getDateOfBirth(), request.getGender(), role);

        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse registerSelf(RegisterRequest request) {
        ensureUserNameAvailable(request.getUserName());

        Role defaultRole = roleRepository.findByName(DEFAULT_SELF_REGISTER_ROLE)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Default role '" + DEFAULT_SELF_REGISTER_ROLE + "' is not configured"));

        User user = buildUser(request.getUserName(), request.getPassword(),
                request.getEmail(), request.getDateOfBirth(), request.getGender(), defaultRole);

        return toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest updatedUser) {

        User existingUser = findEntityById(id);

        if (!existingUser.getUserName().equals(updatedUser.getUserName())
                && userRepository.existsByUserName(updatedUser.getUserName())) {
            throw new IllegalStateException("UserName is taken");
        }

        Role role = roleRepository.findById(updatedUser.getRoleId())
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        existingUser.setUserName(updatedUser.getUserName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
        existingUser.setGender(updatedUser.getGender());
        existingUser.setRole(role);

        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(
                    passwordEncoder.encode(updatedUser.getPassword())
            );
        }

        return toResponse(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User with id " + id + " does not exist");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserResponse setActiveStatus(Long id, boolean active) {
        User user = findEntityById(id);
        user.setActive(active);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    // ─── HELPERS ─────────────────────────────────────────────────

    private void ensureUserNameAvailable(String userName) {
        if (userRepository.existsByUserName(userName)) {
            throw new IllegalStateException("UserName is taken");
        }
    }

    private User buildUser(String userName, String rawPassword, String email,
                           java.time.LocalDate dateOfBirth,
                           com.raya.activitytracking.usermanagement.util.Gender gender,
                           Role role) {
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setDateOfBirth(dateOfBirth);
        user.setGender(gender);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return user;
    }

    private User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .active(user.isActive())
                .build();
    }
}