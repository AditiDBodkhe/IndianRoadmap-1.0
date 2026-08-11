package com.indianroadmap.roadmap.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "roadmaps")
public class RoadmapDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;

    private String name;
    private String description;
    private RoadmapStatus status;
    private List<RoadmapNodeDocument> nodes;
    private List<RoadmapEdgeDocument> edges;
    private RouteSummaryDocument routeSummary;
    private Instant createdAt;
    private Instant updatedAt;

    public RoadmapDocument() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.routeSummary = new RouteSummaryDocument();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoadmapStatus getStatus() {
        return status;
    }

    public void setStatus(RoadmapStatus status) {
        this.status = status;
    }

    public List<RoadmapNodeDocument> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        return nodes;
    }

    public void setNodes(List<RoadmapNodeDocument> nodes) {
        this.nodes = nodes == null ? new ArrayList<>() : nodes;
    }

    public List<RoadmapEdgeDocument> getEdges() {
        if (edges == null) {
            edges = new ArrayList<>();
        }
        return edges;
    }

    public void setEdges(List<RoadmapEdgeDocument> edges) {
        this.edges = edges == null ? new ArrayList<>() : edges;
    }

    public RouteSummaryDocument getRouteSummary() {
        if (routeSummary == null) {
            routeSummary = new RouteSummaryDocument();
        }
        return routeSummary;
    }

    public void setRouteSummary(RouteSummaryDocument routeSummary) {
        this.routeSummary = routeSummary == null ? new RouteSummaryDocument() : routeSummary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
