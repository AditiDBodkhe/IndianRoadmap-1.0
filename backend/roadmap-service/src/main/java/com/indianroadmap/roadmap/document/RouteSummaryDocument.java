package com.indianroadmap.roadmap.document;

public class RouteSummaryDocument {
    private double totalDistanceKm;
    private int totalTravelTimeMinutes;
    private int highestElevationMeters;
    private int lowestElevationMeters;
    private int elevationGainMeters;
    private int nodeCount;
    private int edgeCount;

    public RouteSummaryDocument() {
        this(0.0, 0, 0, 0, 0, 0, 0);
    }

    public RouteSummaryDocument(double totalDistanceKm, int totalTravelTimeMinutes, int highestElevationMeters,
                                int lowestElevationMeters, int elevationGainMeters, int nodeCount, int edgeCount) {
        this.totalDistanceKm = totalDistanceKm;
        this.totalTravelTimeMinutes = totalTravelTimeMinutes;
        this.highestElevationMeters = highestElevationMeters;
        this.lowestElevationMeters = lowestElevationMeters;
        this.elevationGainMeters = elevationGainMeters;
        this.nodeCount = nodeCount;
        this.edgeCount = edgeCount;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public int getTotalTravelTimeMinutes() {
        return totalTravelTimeMinutes;
    }

    public void setTotalTravelTimeMinutes(int totalTravelTimeMinutes) {
        this.totalTravelTimeMinutes = totalTravelTimeMinutes;
    }

    public int getHighestElevationMeters() {
        return highestElevationMeters;
    }

    public void setHighestElevationMeters(int highestElevationMeters) {
        this.highestElevationMeters = highestElevationMeters;
    }

    public int getLowestElevationMeters() {
        return lowestElevationMeters;
    }

    public void setLowestElevationMeters(int lowestElevationMeters) {
        this.lowestElevationMeters = lowestElevationMeters;
    }

    public int getElevationGainMeters() {
        return elevationGainMeters;
    }

    public void setElevationGainMeters(int elevationGainMeters) {
        this.elevationGainMeters = elevationGainMeters;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public void setEdgeCount(int edgeCount) {
        this.edgeCount = edgeCount;
    }
}
