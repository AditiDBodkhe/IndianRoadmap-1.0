package com.indianroadmap.user.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 1, max = 100) String firstName,
        @Size(min = 1, max = 100) String lastName,
        @Size(min = 1, max = 150) String displayName,
        @Size(max = 500) String bio,
        @Size(max = 500) String profileImageUrl,
        @Size(max = 50) String preferredLanguage
) {
}
