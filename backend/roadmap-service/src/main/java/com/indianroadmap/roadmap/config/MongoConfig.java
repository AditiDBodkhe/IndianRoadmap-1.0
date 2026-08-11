package com.indianroadmap.roadmap.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureIndexes() {
        var indexOps = mongoTemplate.indexOps("roadmaps");
        indexOps.createIndex(new Index().on("slug", Sort.Direction.ASC).unique().named("idx_slug_unique"));
        indexOps.createIndex(new Index().on("status", Sort.Direction.ASC).named("idx_status"));
    }
}
