package com.indianroadmap.user.dto.response;

import com.indianroadmap.user.document.AccountStatus;
import com.indianroadmap.user.document.UserDocument;
import com.indianroadmap.user.document.UserRole;

import java.time.Instant;

public record UserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String displayName,
        String bio,
        String profileImageUrl,
        UserRole role,
        AccountStatus status,
        Instant createdAt,
        Instant lastLoginAt
) {

    public static UserResponse from(UserDocument document) {
        return new UserResponse(
                document.getId(),
                document.getEmail(),
                document.getFirstName(),
                document.getLastName(),
                document.getDisplayName(),
                document.getBio(),
                document.getProfileImageUrl(),
                document.getRole(),
                document.getStatus(),
                document.getCreatedAt(),
                document.getLastLoginAt());
    }
}
