package com.raya.activitytracking.usermanagement.controller;

import com.raya.activitytracking.usermanagement.dto.LoginRequest;
import com.raya.activitytracking.usermanagement.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

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
}