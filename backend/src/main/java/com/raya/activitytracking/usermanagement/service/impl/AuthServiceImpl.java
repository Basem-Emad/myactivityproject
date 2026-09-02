package com.raya.activitytracking.usermanagement.service.impl;

import com.raya.activitytracking.usermanagement.dto.request.LoginRequest;
import com.raya.activitytracking.usermanagement.dto.request.RegisterRequest;
import com.raya.activitytracking.usermanagement.dto.response.UserResponse;
import com.raya.activitytracking.usermanagement.security.JwtService;
import com.raya.activitytracking.usermanagement.service.AuthService;
import com.raya.activitytracking.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    public String login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        return userService.registerSelf(request);
    }

    @Override
    public UserResponse getCurrentUser(String username) {
        return userService.getUserByUserName(username);
    }
}