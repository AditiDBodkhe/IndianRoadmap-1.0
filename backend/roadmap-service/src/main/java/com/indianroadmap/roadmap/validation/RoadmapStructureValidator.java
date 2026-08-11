package com.indianroadmap.roadmap.validation;

import com.indianroadmap.roadmap.document.RoadmapDocument;
import com.indianroadmap.roadmap.document.RoadmapEdgeDocument;
import com.indianroadmap.roadmap.document.RoadmapNodeDocument;
import com.indianroadmap.roadmap.dto.request.AddRoadmapEdgeRequest;
import com.indianroadmap.roadmap.exception.InvalidRoadmapEdgeException;
import com.indianroadmap.roadmap.exception.InvalidRoadmapException;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoadmapStructureValidator {

    public void validateForPublishing(RoadmapDocument roadmap) {
        if (roadmap.getNodes().size() < 2) {
            throw new InvalidRoadmapException("Roadmap must contain at least 2 nodes before publishing");
        }
        if (roadmap.getEdges().isEmpty()) {
            throw new InvalidRoadmapException("Roadmap must contain at least 1 edge before publishing");
        }

        Set<String> nodeIds = roadmap.getNodes().stream()
            .map(RoadmapNodeDocument::getNodeId)
            .collect(Collectors.toSet());

        for (RoadmapEdgeDocument edge : roadmap.getEdges()) {
            if (!nodeIds.contains(edge.getFromNodeId()) || !nodeIds.contains(edge.getToNodeId())) {
                throw new InvalidRoadmapException("Roadmap contains edge referencing unknown nodes");
            }
            if (edge.getFromNodeId().equals(edge.getToNodeId())) {
                throw new InvalidRoadmapException("Roadmap contains self-loop edge");
            }
        }

        validateNodeSequences(roadmap.getNodes());
    }

    public void validateEdge(RoadmapDocument roadmap, AddRoadmapEdgeRequest request) {
        Set<String> nodeIds = roadmap.getNodes().stream()
            .map(RoadmapNodeDocument::getNodeId)
            .collect(Collectors.toSet());

        if (!nodeIds.contains(request.fromNodeId())) {
            throw new InvalidRoadmapEdgeException("Source node does not exist in roadmap");
        }
        if (!nodeIds.contains(request.toNodeId())) {
            throw new InvalidRoadmapEdgeException("Target node does not exist in roadmap");
        }
        if (request.fromNodeId().equals(request.toNodeId())) {
            throw new InvalidRoadmapEdgeException("Self-loop edges are not allowed");
        }
        boolean duplicate = roadmap.getEdges().stream()
            .anyMatch(edge -> edge.getFromNodeId().equals(request.fromNodeId()) && edge.getToNodeId().equals(request.toNodeId()));
        if (duplicate) {
            throw new InvalidRoadmapEdgeException("Duplicate edge already exists between nodes");
        }
    }

    public void validateNodeSequences(List<RoadmapNodeDocument> nodes) {
        List<RoadmapNodeDocument> sorted = nodes.stream()
            .sorted(Comparator.comparingInt(RoadmapNodeDocument::getSequence))
            .toList();
        for (int i = 0; i < sorted.size(); i++) {
            int expected = i + 1;
            int actual = sorted.get(i).getSequence();
            if (actual <= 0) {
                throw new InvalidRoadmapException("Node sequence must be positive");
            }
            if (actual != expected) {
                throw new InvalidRoadmapException("Node sequences must be continuous starting at 1");
            }
        }
    }
}
