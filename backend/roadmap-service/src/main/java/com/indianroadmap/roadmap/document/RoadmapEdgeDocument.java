package com.indianroadmap.roadmap.document;

public class RoadmapEdgeDocument {
    private String edgeId;
    private String fromNodeId;
    private String toNodeId;
    private double distanceKm;
    private int estimatedTravelTimeMinutes;
    private RoadType roadType;
    private RouteDifficulty difficulty;

    public RoadmapEdgeDocument() {
    }

    public RoadmapEdgeDocument(String edgeId, String fromNodeId, String toNodeId, double distanceKm,
                               int estimatedTravelTimeMinutes, RoadType roadType, RouteDifficulty difficulty) {
        this.edgeId = edgeId;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.distanceKm = distanceKm;
        this.estimatedTravelTimeMinutes = estimatedTravelTimeMinutes;
        this.roadType = roadType;
        this.difficulty = difficulty;
    }

    public String getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(String edgeId) {
        this.edgeId = edgeId;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(String toNodeId) {
        this.toNodeId = toNodeId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public int getEstimatedTravelTimeMinutes() {
        return estimatedTravelTimeMinutes;
    }

    public void setEstimatedTravelTimeMinutes(int estimatedTravelTimeMinutes) {
        this.estimatedTravelTimeMinutes = estimatedTravelTimeMinutes;
    }

    public RoadType getRoadType() {
        return roadType;
    }

    public void setRoadType(RoadType roadType) {
        this.roadType = roadType;
    }

    public RouteDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(RouteDifficulty difficulty) {
        this.difficulty = difficulty;
    }
}
