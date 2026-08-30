package com.raya.activitytracking.usermanagement.controller;

import com.raya.activitytracking.usermanagement.dto.LoginRequest;
import com.raya.activitytracking.usermanagement.entity.User;
import com.raya.activitytracking.usermanagement.security.JwtService;
import com.raya.activitytracking.usermanagement.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Authentication",
        description = "Authentication and current-user operations"
)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequest loginRequest
            ) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(),
                                loginRequest.getPassword()
                        )
                );
        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String token =
                jwtService.generateToken(userDetails);

        return ResponseEntity.ok(token);

    }
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok(
                userService.getUserByUserName(username)
        );
    }

}