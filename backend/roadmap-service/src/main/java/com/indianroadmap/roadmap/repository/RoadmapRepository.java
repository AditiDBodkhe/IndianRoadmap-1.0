package com.indianroadmap.roadmap.repository;

import com.indianroadmap.roadmap.document.RoadmapDocument;
import com.indianroadmap.roadmap.document.RoadmapStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoadmapRepository extends MongoRepository<RoadmapDocument, String> {
    Optional<RoadmapDocument> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, String id);
    Page<RoadmapDocument> findByStatus(RoadmapStatus status, Pageable pageable);
}
