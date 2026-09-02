package com.raya.activitytracking.usermanagement.service;

import com.raya.activitytracking.usermanagement.dto.request.RegisterRequest;
import com.raya.activitytracking.usermanagement.dto.request.UserRequest;
import com.raya.activitytracking.usermanagement.dto.response.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    List<UserResponse> getUsers();
    UserResponse getUserById(Long id);
    UserResponse getUserByUserName(String userName);
    UserResponse addNewUser(UserRequest request);
    UserResponse registerSelf(RegisterRequest request);
    UserResponse updateUser(Long id, UserRequest updatedUser);
    void deleteUser(Long id);
    Page<UserResponse> getAllUsers(int page, int size);
    UserResponse setActiveStatus(Long id, boolean active);
}