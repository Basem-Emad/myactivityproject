package com.activitytracking.user.controller;

import com.activitytracking.security.JwtUtil;
import com.activitytracking.user.dto.request.LoginRequestDto;
import com.activitytracking.user.dto.response.LoginResponseDto;
import com.activitytracking.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        String token = authService.authenticate(request.getEmail(), request.getPassword());
        String role = jwtUtil.extractRole(token);

        LoginResponseDto response = new LoginResponseDto(token, request.getEmail(), role);
        return ResponseEntity.ok(response);
    }
}