package com.indianroadmap.user.service;

import com.indianroadmap.user.document.UserDocument;
import com.indianroadmap.user.document.UserPreferences;
import com.indianroadmap.user.dto.request.UpdatePreferencesRequest;
import com.indianroadmap.user.dto.request.UpdateProfileRequest;
import com.indianroadmap.user.dto.response.UserPreferencesResponse;
import com.indianroadmap.user.dto.response.UserResponse;
import com.indianroadmap.user.exception.UserNotFoundException;
import com.indianroadmap.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Clock clock;

    public UserServiceImpl(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Override
    public UserResponse getCurrentUser(String userId) {
        return UserResponse.from(getUser(userId));
    }

    @Override
    public UserResponse updateCurrentUser(String userId, UpdateProfileRequest request) {
        UserDocument user = getUser(userId);

        if (request.firstName() != null) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName().trim());
        }
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName().trim());
        }
        if (request.bio() != null) {
            user.setBio(trimToNull(request.bio()));
        }
        if (request.profileImageUrl() != null) {
            user.setProfileImageUrl(trimToNull(request.profileImageUrl()));
        }
        if (request.preferredLanguage() != null) {
            user.setPreferredLanguage(trimToNull(request.preferredLanguage()));
        }
        user.setUpdatedAt(Instant.now(clock));

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public UserPreferencesResponse getPreferences(String userId) {
        return UserPreferencesResponse.from(getUser(userId).getPreferences());
    }

    @Override
    public UserPreferencesResponse updatePreferences(String userId, UpdatePreferencesRequest request) {
        UserDocument user = getUser(userId);
        UserPreferences preferences = new UserPreferences(
                safeList(request.preferredMoods()),
                safeList(request.preferredInterests()),
                safeList(request.preferredTravelStyles()),
                safeList(request.preferredRegions()),
                safeList(request.preferredLanguages()),
                request.defaultTripDurationDays(),
                request.budgetRange());
        user.setPreferences(preferences);
        user.setUpdatedAt(Instant.now(clock));
        userRepository.save(user);
        return UserPreferencesResponse.from(preferences);
    }

    private UserDocument getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private List<String> safeList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(value -> value == null ? null : value.trim())
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private String trimToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
