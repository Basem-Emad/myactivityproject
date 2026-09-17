package com.activitytracking.user.service;

import com.activitytracking.user.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto login(String email, String password);
    AuthResponseDto refresh(String refreshToken);
    void logout(String refreshToken);
}