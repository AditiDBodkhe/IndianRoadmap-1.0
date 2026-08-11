package com.indianroadmap.roadmap.document;

import org.springframework.data.mongodb.core.mapping.Field;

public class RoadmapNodeDocument {
    @Field("nodeId")
    private String nodeId;

    @Field("destinationId")
    private String destinationId;

    @Field("sequence")
    private int sequence;

    @Field("label")
    private String label;

    @Field("role")
    private RoadmapNodeRole role;

    @Field("elevationMeters")
    private int elevationMeters;

    public RoadmapNodeDocument() {
    }

    public RoadmapNodeDocument(String nodeId, String destinationId, int sequence, String label, RoadmapNodeRole role, int elevationMeters) {
        this.nodeId = nodeId;
        this.destinationId = destinationId;
        this.sequence = sequence;
        this.label = label;
        this.role = role;
        this.elevationMeters = elevationMeters;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public RoadmapNodeRole getRole() {
        return role;
    }

    public void setRole(RoadmapNodeRole role) {
        this.role = role;
    }

    public int getElevationMeters() {
        return elevationMeters;
    }

    public void setElevationMeters(int elevationMeters) {
        this.elevationMeters = elevationMeters;
    }
}
