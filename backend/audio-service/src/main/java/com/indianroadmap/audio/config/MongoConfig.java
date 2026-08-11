package com.indianroadmap.audio.config;

import com.indianroadmap.audio.document.AudioAssetDocument;
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
        var indexOps = mongoTemplate.indexOps(AudioAssetDocument.class);
        createIndexSafely(indexOps, new Index().on("storyId", Sort.Direction.ASC));
        createIndexSafely(indexOps, new Index().on("sectionId", Sort.Direction.ASC));
        createIndexSafely(indexOps, new Index().on("status", Sort.Direction.ASC));
        createIndexSafely(indexOps, new Index().on("language", Sort.Direction.ASC));
        createIndexSafely(indexOps, new Index().on("contentHash", Sort.Direction.ASC));
        createIndexSafely(indexOps, new CompoundIndexDefinition(
                new Document("storyId", 1).append("sectionId", 1).append("language", 1).append("version", 1)));
    }

    private void createIndexSafely(IndexOperations indexOps, IndexDefinition definition) {
        try {
            indexOps.createIndex(definition);
        } catch (DataIntegrityViolationException ex) {
            String message = ex.getMessage();
            if (message == null || !message.contains("Index already exists with a different name")) {
                throw ex;
            }
        }
    }
}
