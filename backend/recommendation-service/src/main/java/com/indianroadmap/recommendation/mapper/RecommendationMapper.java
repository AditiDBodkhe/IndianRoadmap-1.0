package com.indianroadmap.recommendation.mapper;

import com.indianroadmap.recommendation.document.RecommendationProfileDocument;
import com.indianroadmap.recommendation.dto.request.RecommendationProfileRequest;
import com.indianroadmap.recommendation.dto.response.RecommendationProfileResponse;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
public class RecommendationMapper {

    private final Clock clock;

    public RecommendationMapper(Clock clock) {
        this.clock = clock;
    }

    public RecommendationProfileDocument toDocument(RecommendationProfileRequest request) {
        var doc = new RecommendationProfileDocument();
        doc.setDestinationId(request.destinationId());
        doc.setMoods(request.moods() != null ? List.copyOf(request.moods()) : List.of());
        doc.setInterests(request.interests() != null ? List.copyOf(request.interests()) : List.of());
        doc.setTravelStyles(request.travelStyles() != null ? List.copyOf(request.travelStyles()) : List.of());
        doc.setRegions(request.regions() != null ? List.copyOf(request.regions()) : List.of());
        doc.setIdealDurationMin(request.idealDurationMin());
        doc.setIdealDurationMax(request.idealDurationMax());
        doc.setBudgetMin(request.budgetMin());
        doc.setBudgetMax(request.budgetMax());
        doc.setSeasonTags(request.seasonTags() != null ? List.copyOf(request.seasonTags()) : List.of());
        doc.setDifficulty(request.difficulty());
        doc.setWeight(1.0);
        Instant now = Instant.now(clock);
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);
        return doc;
    }

    public void updateDocument(RecommendationProfileDocument doc, RecommendationProfileRequest request) {
        doc.setMoods(request.moods() != null ? List.copyOf(request.moods()) : List.of());
        doc.setInterests(request.interests() != null ? List.copyOf(request.interests()) : List.of());
        doc.setTravelStyles(request.travelStyles() != null ? List.copyOf(request.travelStyles()) : List.of());
        doc.setRegions(request.regions() != null ? List.copyOf(request.regions()) : List.of());
        doc.setIdealDurationMin(request.idealDurationMin());
        doc.setIdealDurationMax(request.idealDurationMax());
        doc.setBudgetMin(request.budgetMin());
        doc.setBudgetMax(request.budgetMax());
        doc.setSeasonTags(request.seasonTags() != null ? List.copyOf(request.seasonTags()) : List.of());
        doc.setDifficulty(request.difficulty());
        doc.setUpdatedAt(Instant.now(clock));
    }

    public RecommendationProfileResponse toResponse(RecommendationProfileDocument doc) {
        return new RecommendationProfileResponse(
                doc.getId(),
                doc.getDestinationId(),
                doc.getMoods() != null ? List.copyOf(doc.getMoods()) : List.of(),
                doc.getInterests() != null ? List.copyOf(doc.getInterests()) : List.of(),
                doc.getTravelStyles() != null ? List.copyOf(doc.getTravelStyles()) : List.of(),
                doc.getRegions() != null ? List.copyOf(doc.getRegions()) : List.of(),
                doc.getIdealDurationMin(),
                doc.getIdealDurationMax(),
                doc.getBudgetMin(),
                doc.getBudgetMax(),
                doc.getSeasonTags() != null ? List.copyOf(doc.getSeasonTags()) : List.of(),
                doc.getDifficulty(),
                doc.getWeight(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}
