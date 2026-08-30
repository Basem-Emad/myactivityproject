package com.activitytracking.user.service;

public interface AuthService {
    String authenticate(String email, String password);
}