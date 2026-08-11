package com.indianroadmap.user.document;

import java.util.List;

public class UserPreferences {

    private List<String> preferredMoods;
    private List<String> preferredInterests;
    private List<String> preferredTravelStyles;
    private List<String> preferredRegions;
    private List<String> preferredLanguages;
    private Integer defaultTripDurationDays;
    private BudgetRange budgetRange;

    public UserPreferences() {
        this.preferredMoods = List.of();
        this.preferredInterests = List.of();
        this.preferredTravelStyles = List.of();
        this.preferredRegions = List.of();
        this.preferredLanguages = List.of();
    }

    public UserPreferences(List<String> preferredMoods,
                           List<String> preferredInterests,
                           List<String> preferredTravelStyles,
                           List<String> preferredRegions,
                           List<String> preferredLanguages,
                           Integer defaultTripDurationDays,
                           BudgetRange budgetRange) {
        this.preferredMoods = preferredMoods == null ? List.of() : List.copyOf(preferredMoods);
        this.preferredInterests = preferredInterests == null ? List.of() : List.copyOf(preferredInterests);
        this.preferredTravelStyles = preferredTravelStyles == null ? List.of() : List.copyOf(preferredTravelStyles);
        this.preferredRegions = preferredRegions == null ? List.of() : List.copyOf(preferredRegions);
        this.preferredLanguages = preferredLanguages == null ? List.of() : List.copyOf(preferredLanguages);
        this.defaultTripDurationDays = defaultTripDurationDays;
        this.budgetRange = budgetRange;
    }

    public List<String> getPreferredMoods() {
        return preferredMoods;
    }

    public void setPreferredMoods(List<String> preferredMoods) {
        this.preferredMoods = preferredMoods == null ? List.of() : List.copyOf(preferredMoods);
    }

    public List<String> getPreferredInterests() {
        return preferredInterests;
    }

    public void setPreferredInterests(List<String> preferredInterests) {
        this.preferredInterests = preferredInterests == null ? List.of() : List.copyOf(preferredInterests);
    }

    public List<String> getPreferredTravelStyles() {
        return preferredTravelStyles;
    }

    public void setPreferredTravelStyles(List<String> preferredTravelStyles) {
        this.preferredTravelStyles = preferredTravelStyles == null ? List.of() : List.copyOf(preferredTravelStyles);
    }

    public List<String> getPreferredRegions() {
        return preferredRegions;
    }

    public void setPreferredRegions(List<String> preferredRegions) {
        this.preferredRegions = preferredRegions == null ? List.of() : List.copyOf(preferredRegions);
    }

    public List<String> getPreferredLanguages() {
        return preferredLanguages;
    }

    public void setPreferredLanguages(List<String> preferredLanguages) {
        this.preferredLanguages = preferredLanguages == null ? List.of() : List.copyOf(preferredLanguages);
    }

    public Integer getDefaultTripDurationDays() {
        return defaultTripDurationDays;
    }

    public void setDefaultTripDurationDays(Integer defaultTripDurationDays) {
        this.defaultTripDurationDays = defaultTripDurationDays;
    }

    public BudgetRange getBudgetRange() {
        return budgetRange;
    }

    public void setBudgetRange(BudgetRange budgetRange) {
        this.budgetRange = budgetRange;
    }
}
