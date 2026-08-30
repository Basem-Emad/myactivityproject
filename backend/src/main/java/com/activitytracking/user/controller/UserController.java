package com.activitytracking.user.controller;

import com.activitytracking.user.dto.request.AssignRoleRequestDto;
import com.activitytracking.user.dto.request.CreateUserRequestDto;
import com.activitytracking.user.dto.request.UpdateUserRequestDto;
import com.activitytracking.user.dto.response.UserResponseDto;
import com.activitytracking.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody CreateUserRequestDto request) {
        UserResponseDto created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRequestDto request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDto> assignRole(
            @PathVariable Long id, @Valid @RequestBody AssignRoleRequestDto request) {
        return ResponseEntity.ok(userService.assignRole(id, request));
    }
}