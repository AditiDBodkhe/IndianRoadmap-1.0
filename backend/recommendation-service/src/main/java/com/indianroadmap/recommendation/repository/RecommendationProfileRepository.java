package com.indianroadmap.recommendation.repository;

import com.indianroadmap.recommendation.document.Interest;
import com.indianroadmap.recommendation.document.Mood;
import com.indianroadmap.recommendation.document.RecommendationProfileDocument;
import com.indianroadmap.recommendation.document.TravelStyle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RecommendationProfileRepository extends MongoRepository<RecommendationProfileDocument, String> {

    Optional<RecommendationProfileDocument> findByDestinationId(String destinationId);

    List<RecommendationProfileDocument> findByMoodsContaining(Mood mood);

    List<RecommendationProfileDocument> findByInterestsContaining(Interest interest);

    List<RecommendationProfileDocument> findByRegionsContaining(String region);

    boolean existsByDestinationId(String destinationId);

    void deleteByDestinationId(String destinationId);
}
