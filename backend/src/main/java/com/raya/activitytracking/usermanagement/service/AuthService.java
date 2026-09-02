package com.raya.activitytracking.usermanagement.service;

import com.raya.activitytracking.usermanagement.dto.request.LoginRequest;
import com.raya.activitytracking.usermanagement.dto.request.RegisterRequest;
import com.raya.activitytracking.usermanagement.dto.response.UserResponse;

public interface AuthService {
    String login(LoginRequest request);
    UserResponse register(RegisterRequest request);
    UserResponse getCurrentUser(String username);
}