package com.indianroadmap.user.dto.response;

import com.indianroadmap.user.document.BudgetRange;
import com.indianroadmap.user.document.UserPreferences;

import java.util.List;

public record UserPreferencesResponse(
        List<String> preferredMoods,
        List<String> preferredInterests,
        List<String> preferredTravelStyles,
        List<String> preferredRegions,
        List<String> preferredLanguages,
        Integer defaultTripDurationDays,
        BudgetRange budgetRange
) {

    public UserPreferencesResponse {
        preferredMoods = preferredMoods == null ? List.of() : List.copyOf(preferredMoods);
        preferredInterests = preferredInterests == null ? List.of() : List.copyOf(preferredInterests);
        preferredTravelStyles = preferredTravelStyles == null ? List.of() : List.copyOf(preferredTravelStyles);
        preferredRegions = preferredRegions == null ? List.of() : List.copyOf(preferredRegions);
        preferredLanguages = preferredLanguages == null ? List.of() : List.copyOf(preferredLanguages);
    }

    public static UserPreferencesResponse empty() {
        return new UserPreferencesResponse(List.of(), List.of(), List.of(), List.of(), List.of(), null, null);
    }

    public static UserPreferencesResponse from(UserPreferences preferences) {
        if (preferences == null) {
            return empty();
        }
        return new UserPreferencesResponse(
                preferences.getPreferredMoods(),
                preferences.getPreferredInterests(),
                preferences.getPreferredTravelStyles(),
                preferences.getPreferredRegions(),
                preferences.getPreferredLanguages(),
                preferences.getDefaultTripDurationDays(),
                preferences.getBudgetRange());
    }
}
