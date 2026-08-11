package com.indianroadmap.user.service;

import com.indianroadmap.user.dto.request.LoginRequest;
import com.indianroadmap.user.dto.request.LogoutRequest;
import com.indianroadmap.user.dto.request.RefreshTokenRequest;
import com.indianroadmap.user.dto.request.RegisterRequest;
import com.indianroadmap.user.dto.response.AuthResponse;
import com.indianroadmap.user.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(LogoutRequest request);
}
