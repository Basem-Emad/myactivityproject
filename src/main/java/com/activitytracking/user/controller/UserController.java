package com.activitytracking.user.controller;

import com.activitytracking.user.dto.request.AssignRoleRequestDto;
import com.activitytracking.user.dto.request.CreateUserRequestDto;
import com.activitytracking.user.dto.request.UpdateUserRequestDto;
import com.activitytracking.user.dto.response.UserResponseDto;
import com.activitytracking.user.constants.PermissionNames;
import com.activitytracking.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.USER_READ + "')")
    public ResponseEntity<List<UserResponseDto>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.USER_READ + "')")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionNames.USER_CREATE + "')")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        UserResponseDto created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.USER_UPDATE + "')")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRequestDto request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('" + PermissionNames.USER_UPDATE + "')")
    public ResponseEntity<UserResponseDto> assignRole(
            @PathVariable Long id, @Valid @RequestBody AssignRoleRequestDto request) {
        return ResponseEntity.ok(userService.assignRole(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionNames.USER_DELETE + "')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}