package com.indianroadmap.recommendation.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "recommendation_profiles")
public class RecommendationProfileDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String destinationId;

    private List<Mood> moods;
    private List<Interest> interests;
    private List<TravelStyle> travelStyles;
    private List<String> regions;
    private int idealDurationMin;
    private int idealDurationMax;
    private int budgetMin;
    private int budgetMax;
    private List<Season> seasonTags;
    private String difficulty;
    private double weight;
    private Instant createdAt;
    private Instant updatedAt;

    public RecommendationProfileDocument() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDestinationId() { return destinationId; }
    public void setDestinationId(String destinationId) { this.destinationId = destinationId; }

    public List<Mood> getMoods() { return moods; }
    public void setMoods(List<Mood> moods) { this.moods = moods; }

    public List<Interest> getInterests() { return interests; }
    public void setInterests(List<Interest> interests) { this.interests = interests; }

    public List<TravelStyle> getTravelStyles() { return travelStyles; }
    public void setTravelStyles(List<TravelStyle> travelStyles) { this.travelStyles = travelStyles; }

    public List<String> getRegions() { return regions; }
    public void setRegions(List<String> regions) { this.regions = regions; }

    public int getIdealDurationMin() { return idealDurationMin; }
    public void setIdealDurationMin(int idealDurationMin) { this.idealDurationMin = idealDurationMin; }

    public int getIdealDurationMax() { return idealDurationMax; }
    public void setIdealDurationMax(int idealDurationMax) { this.idealDurationMax = idealDurationMax; }

    public int getBudgetMin() { return budgetMin; }
    public void setBudgetMin(int budgetMin) { this.budgetMin = budgetMin; }

    public int getBudgetMax() { return budgetMax; }
    public void setBudgetMax(int budgetMax) { this.budgetMax = budgetMax; }

    public List<Season> getSeasonTags() { return seasonTags; }
    public void setSeasonTags(List<Season> seasonTags) { this.seasonTags = seasonTags; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
