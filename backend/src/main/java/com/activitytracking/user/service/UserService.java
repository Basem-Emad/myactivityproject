package com.activitytracking.user.service;

import com.activitytracking.user.dto.request.AssignRoleRequestDto;
import com.activitytracking.user.dto.request.CreateUserRequestDto;
import com.activitytracking.user.dto.request.UpdateUserRequestDto;
import com.activitytracking.user.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {
    List<UserResponseDto> listUsers();
    UserResponseDto createUser(CreateUserRequestDto request);
    UserResponseDto updateUser(Long id, UpdateUserRequestDto request);
    UserResponseDto assignRole(Long id, AssignRoleRequestDto request);
}