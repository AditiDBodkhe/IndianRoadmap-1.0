package com.indianroadmap.roadmap.mapper;

import com.indianroadmap.roadmap.document.RoadmapDocument;
import com.indianroadmap.roadmap.document.RoadmapEdgeDocument;
import com.indianroadmap.roadmap.document.RoadmapNodeDocument;
import com.indianroadmap.roadmap.document.RoadmapStatus;
import com.indianroadmap.roadmap.document.RouteSummaryDocument;
import com.indianroadmap.roadmap.dto.request.CreateRoadmapRequest;
import com.indianroadmap.roadmap.dto.response.RoadmapEdgeResponse;
import com.indianroadmap.roadmap.dto.response.RoadmapNodeResponse;
import com.indianroadmap.roadmap.dto.response.RoadmapResponse;
import com.indianroadmap.roadmap.dto.response.RoadmapSummaryResponse;
import com.indianroadmap.roadmap.dto.response.RouteSummaryResponse;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

@Component
public class RoadmapMapper {

    public RoadmapDocument mapToDocument(CreateRoadmapRequest request, Clock clock) {
        Instant now = Instant.now(clock);
        RoadmapDocument document = new RoadmapDocument();
        document.setSlug(normalizeSlug(request.slug()));
        document.setName(request.name().trim());
        document.setDescription(request.description());
        document.setStatus(request.status() == null ? RoadmapStatus.DRAFT : request.status());
        document.setRouteSummary(new RouteSummaryDocument());
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        return document;
    }

    public RoadmapResponse mapToResponse(RoadmapDocument roadmap) {
        return new RoadmapResponse(
            roadmap.getId(),
            roadmap.getSlug(),
            roadmap.getName(),
            roadmap.getDescription(),
            roadmap.getStatus(),
            roadmap.getNodes().stream().map(this::mapNodeToResponse).toList(),
            roadmap.getEdges().stream().map(this::mapEdgeToResponse).toList(),
            mapRouteSummaryToResponse(roadmap.getRouteSummary()),
            roadmap.getCreatedAt(),
            roadmap.getUpdatedAt()
        );
    }

    public RoadmapSummaryResponse mapToSummaryResponse(RoadmapDocument roadmap) {
        RouteSummaryDocument summary = roadmap.getRouteSummary() != null
            ? roadmap.getRouteSummary() : new RouteSummaryDocument();
        return new RoadmapSummaryResponse(
            roadmap.getId(),
            roadmap.getSlug(),
            roadmap.getName(),
            roadmap.getDescription(),
            roadmap.getStatus(),
            roadmap.getNodes().size(),
            roadmap.getEdges().size(),
            summary.getTotalDistanceKm(),
            roadmap.getCreatedAt(),
            roadmap.getUpdatedAt()
        );
    }

    public RoadmapNodeResponse mapNodeToResponse(RoadmapNodeDocument node) {
        return new RoadmapNodeResponse(
            node.getNodeId(),
            node.getDestinationId(),
            node.getSequence(),
            node.getLabel(),
            node.getRole(),
            node.getElevationMeters()
        );
    }

    public RoadmapEdgeResponse mapEdgeToResponse(RoadmapEdgeDocument edge) {
        return new RoadmapEdgeResponse(
            edge.getEdgeId(),
            edge.getFromNodeId(),
            edge.getToNodeId(),
            edge.getDistanceKm(),
            edge.getEstimatedTravelTimeMinutes(),
            edge.getRoadType(),
            edge.getDifficulty()
        );
    }

    public RouteSummaryResponse mapRouteSummaryToResponse(RouteSummaryDocument summary) {
        RouteSummaryDocument safeSummary = summary == null ? new RouteSummaryDocument() : summary;
        return new RouteSummaryResponse(
            safeSummary.getTotalDistanceKm(),
            safeSummary.getTotalTravelTimeMinutes(),
            safeSummary.getHighestElevationMeters(),
            safeSummary.getLowestElevationMeters(),
            safeSummary.getElevationGainMeters(),
            safeSummary.getNodeCount(),
            safeSummary.getEdgeCount()
        );
    }

    public String normalizeSlug(String slug) {
        if (slug == null) {
            return null;
        }
        return slug.trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("\s+", "-");
    }
}
