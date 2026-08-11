package com.indianroadmap.recommendation.config;

import com.indianroadmap.recommendation.document.RecommendationProfileDocument;
import org.bson.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;

@Component
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        var indexOps = mongoTemplate.indexOps(RecommendationProfileDocument.class);
        createIndexSafely(indexOps, new Index().on("destinationId", Sort.Direction.ASC).unique());
        createIndexSafely(indexOps, new Index().on("moods", Sort.Direction.ASC));
        createIndexSafely(indexOps, new Index().on("interests", Sort.Direction.ASC));
        createIndexSafely(indexOps, new Index().on("regions", Sort.Direction.ASC));
    }

    private void createIndexSafely(IndexOperations indexOps, IndexDefinition def) {
        try {
            indexOps.createIndex(def);
        } catch (DataIntegrityViolationException ex) {
            String msg = ex.getMessage();
            if (msg == null || !msg.contains("Index already exists with a different name")) throw ex;
        }
    }
}
