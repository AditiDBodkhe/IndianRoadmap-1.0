package com.indianroadmap.story.config;

import com.indianroadmap.story.document.StoryDocument;
import org.bson.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.stereotype.Component;

@Component
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        var indexOps = mongoTemplate.indexOps(StoryDocument.class);
        createIndexSafely(indexOps, new Index().on("slug", Sort.Direction.ASC).unique());
        createIndexSafely(indexOps, new Index().on("destinationId", Sort.Direction.ASC));
        createIndexSafely(indexOps, new Index().on("status", Sort.Direction.ASC));
        createIndexSafely(indexOps, new CompoundIndexDefinition(new Document("destinationId", 1).append("status", 1)));
    }

    private void createIndexSafely(org.springframework.data.mongodb.core.index.IndexOperations indexOps, IndexDefinition definition) {
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
