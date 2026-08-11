package com.indianroadmap.destination.repository;

import com.indianroadmap.destination.document.DestinationDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DestinationRepository extends MongoRepository<DestinationDocument, String> {
    Optional<DestinationDocument> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, String id);
}
