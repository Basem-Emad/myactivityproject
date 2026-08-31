package com.raya.activitytracking.usermanagement.controller;

import com.raya.activitytracking.usermanagement.dto.request.UserRequest;
import com.raya.activitytracking.usermanagement.service.UserService;
import com.raya.activitytracking.usermanagement.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(path = "api/v1/user")
@Tag(
        name = "User management",
        description = "Create & Get users"
)
public class UserController {
private final UserService userService;

    @GetMapping()
    @Operation(summary = "List all users")
    @PreAuthorize("hasAuthority('Read_User')")
    public List<User> getUsers(){
        return userService.getUsers();
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get a single user by ID")
    @PreAuthorize("hasAuthority('Read_User')")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/getUserByUsername")
    @Operation(summary = "Get a user by username")
    @PreAuthorize("hasAuthority('Read_User')")
    public User getUserByName(@RequestParam String userName){
    return userService.getUserByUserName(userName);
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    @PreAuthorize("hasAuthority('Create_User')")
    public void createNewUser(@RequestBody UserRequest request){
        userService.addNewUser(request);
    }

    @DeleteMapping(
            path = "{userId}")
    @Operation(summary = "Delete a user by ID")
    @PreAuthorize("hasAuthority('Delete_User')")
    public void deleteUser(
            @PathVariable("userId")Long Id){
            userService.deleteUser(Id);
    }

    @PutMapping("/updateUser/{id}")
    @Operation(summary = "Update an existing user")
    @PreAuthorize("hasAuthority('Update_User')")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody UserRequest updatedUser) {

        return userService.updateUser(id, updatedUser);
    }

    @GetMapping("/with_pagination")
    @Operation(summary = "List users with pagination")
    @PreAuthorize("hasAuthority('Read_User')")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                userService.getAllUsers(page, size)
        );
    }

    @GetMapping("/authorities")
    @Operation(summary = "Get the current authenticated user's authorities")
    public Object authorities(Authentication authentication ) {
        return authentication.getAuthorities();
    }
}
