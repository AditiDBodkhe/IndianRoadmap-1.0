package com.indianroadmap.user.repository;

import com.indianroadmap.user.document.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByEmail(String email);

    boolean existsByEmail(String email);
}
