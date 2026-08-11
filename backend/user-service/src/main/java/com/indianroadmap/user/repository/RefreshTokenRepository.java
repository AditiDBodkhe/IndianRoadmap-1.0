package com.indianroadmap.user.repository;

import com.indianroadmap.user.document.RefreshTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshTokenDocument, String> {

    Optional<RefreshTokenDocument> findByTokenHash(String tokenHash);

    List<RefreshTokenDocument> findByUserId(String userId);
}
