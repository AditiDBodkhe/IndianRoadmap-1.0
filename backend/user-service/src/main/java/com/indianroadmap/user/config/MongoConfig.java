package com.indianroadmap.user.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
public class MongoConfig {

    @Bean
    ApplicationListener<ApplicationReadyEvent> mongoIndexes(MongoTemplate mongoTemplate) {
        return event -> {
            createIndex(mongoTemplate.indexOps("users"),
                    new Index().on("email", Sort.Direction.ASC).unique().named("email"));
            createIndex(mongoTemplate.indexOps("refresh_tokens"),
                    new Index().on("tokenHash", Sort.Direction.ASC).unique().named("tokenHash"));
            createIndex(mongoTemplate.indexOps("refresh_tokens"),
                    new Index().on("userId", Sort.Direction.ASC).named("userId"));
            createIndex(mongoTemplate.indexOps("refresh_tokens"),
                    new Index().on("expiresAt", Sort.Direction.ASC).named("expiresAt"));
        };
    }

    private void createIndex(IndexOperations indexOperations, Index index) {
        try {
            indexOperations.createIndex(index);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage() == null || !ex.getMessage().contains("IndexOptionsConflict")) {
                throw ex;
            }
        }
    }
}
