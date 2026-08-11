package com.indianroadmap.story.repository;

import com.indianroadmap.story.document.StoryDocument;
import com.indianroadmap.story.document.StoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StoryRepository extends MongoRepository<StoryDocument, String> {

    Optional<StoryDocument> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<StoryDocument> findByDestinationId(String destinationId);

    Page<StoryDocument> findByStatus(StoryStatus status, Pageable pageable);

    Page<StoryDocument> findByDestinationIdAndStatus(String destinationId, StoryStatus status, Pageable pageable);

    List<StoryDocument> findByDestinationIdAndStatus(String destinationId, StoryStatus status);
}
