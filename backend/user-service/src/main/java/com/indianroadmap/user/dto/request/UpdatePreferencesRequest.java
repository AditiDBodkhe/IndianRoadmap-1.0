package com.indianroadmap.user.dto.request;

import com.indianroadmap.user.document.BudgetRange;

import java.util.List;

public record UpdatePreferencesRequest(
        List<String> preferredMoods,
        List<String> preferredInterests,
        List<String> preferredTravelStyles,
        List<String> preferredRegions,
        List<String> preferredLanguages,
        Integer defaultTripDurationDays,
        BudgetRange budgetRange
) {
}
