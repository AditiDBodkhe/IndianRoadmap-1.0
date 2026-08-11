package com.indianroadmap.destination.config;

import com.indianroadmap.destination.document.DestinationDocument;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        var indexOps = mongoTemplate.indexOps(DestinationDocument.class);
        indexOps.createIndex(new Index().on("state", Sort.Direction.ASC).named("idx_state"));
        indexOps.createIndex(new Index().on("region", Sort.Direction.ASC).named("idx_region"));
        indexOps.createIndex(new Index().on("categories", Sort.Direction.ASC).named("idx_categories"));
        indexOps.createIndex(new Index().on("moods", Sort.Direction.ASC).named("idx_moods"));
    }
}
