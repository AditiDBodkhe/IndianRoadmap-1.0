package com.indianroadmap.user.service;

import com.indianroadmap.user.dto.request.UpdatePreferencesRequest;
import com.indianroadmap.user.dto.request.UpdateProfileRequest;
import com.indianroadmap.user.dto.response.UserPreferencesResponse;
import com.indianroadmap.user.dto.response.UserResponse;

public interface UserService {

    UserResponse getCurrentUser(String userId);

    UserResponse updateCurrentUser(String userId, UpdateProfileRequest request);

    UserPreferencesResponse getPreferences(String userId);

    UserPreferencesResponse updatePreferences(String userId, UpdatePreferencesRequest request);
}
