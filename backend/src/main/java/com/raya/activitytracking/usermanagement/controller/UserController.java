package com.raya.activitytracking.usermanagement.controller;

import com.raya.activitytracking.usermanagement.dto.request.UserRequest;
import com.raya.activitytracking.usermanagement.dto.response.UserResponse;
import com.raya.activitytracking.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "api/v1/user")
@Tag(name = "User management", description = "Create & Get users")
public class UserController {

    private final UserService userService;

    @GetMapping()
    @Operation(summary = "List all users")
    @PreAuthorize("hasAuthority('Read_User')")
    public List<UserResponse> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single user by ID")
    @PreAuthorize("hasAuthority('Read_User')")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/getUserByUsername")
    @Operation(summary = "Get a user by username")
    @PreAuthorize("hasAuthority('Read_User')")
    public UserResponse getUserByName(@RequestParam String userName) {
        return userService.getUserByUserName(userName);
    }

    @PostMapping
    @Operation(summary = "Create a new user (admin only, explicit role)")
    @PreAuthorize("hasAuthority('Create_User')")
    public UserResponse createNewUser(@Valid @RequestBody UserRequest request) {
        return userService.addNewUser(request);
    }

    @DeleteMapping(path = "{userId}")
    @Operation(summary = "Delete a user by ID")
    @PreAuthorize("hasAuthority('Delete_User')")
    public void deleteUser(@PathVariable("userId") Long id) {
        userService.deleteUser(id);
    }

    @PutMapping("/updateUser/{id}")
    @Operation(summary = "Update an existing user")
    @PreAuthorize("hasAuthority('Update_User')")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest updatedUser) {
        return userService.updateUser(id, updatedUser);
    }

    @GetMapping("/with_pagination")
    @Operation(summary = "List users with pagination")
    @PreAuthorize("hasAuthority('Read_User')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("/authorities")
    @Operation(summary = "Get the current authenticated user's authorities")
    public Object authorities(Authentication authentication) {
        return authentication.getAuthorities();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate or deactivate a user account")
    @PreAuthorize("hasAuthority('Update_User')")
    public UserResponse setActiveStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return userService.setActiveStatus(id, active);
    }
}