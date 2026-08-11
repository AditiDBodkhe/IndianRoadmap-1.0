package com.indianroadmap.roadmap.service;

import com.indianroadmap.roadmap.client.DestinationClient;
import com.indianroadmap.roadmap.config.RoadmapProperties;
import com.indianroadmap.roadmap.document.RoadmapDocument;
import com.indianroadmap.roadmap.document.RoadmapEdgeDocument;
import com.indianroadmap.roadmap.document.RoadmapNodeDocument;
import com.indianroadmap.roadmap.document.RoadmapStatus;
import com.indianroadmap.roadmap.document.RouteSummaryDocument;
import com.indianroadmap.roadmap.dto.request.AddRoadmapEdgeRequest;
import com.indianroadmap.roadmap.dto.request.AddRoadmapNodeRequest;
import com.indianroadmap.roadmap.dto.request.CreateRoadmapRequest;
import com.indianroadmap.roadmap.dto.request.ReorderRoadmapNodesRequest;
import com.indianroadmap.roadmap.dto.request.UpdateRoadmapEdgeRequest;
import com.indianroadmap.roadmap.dto.request.UpdateRoadmapNodeRequest;
import com.indianroadmap.roadmap.dto.request.UpdateRoadmapRequest;
import com.indianroadmap.roadmap.dto.response.RoadmapEdgeResponse;
import com.indianroadmap.roadmap.dto.response.RoadmapNodeResponse;
import com.indianroadmap.roadmap.dto.response.RoadmapResponse;
import com.indianroadmap.roadmap.dto.response.RoadmapSummaryResponse;
import com.indianroadmap.roadmap.exception.DuplicateRoadmapException;
import com.indianroadmap.roadmap.exception.InvalidRoadmapEdgeException;
import com.indianroadmap.roadmap.exception.InvalidRoadmapException;
import com.indianroadmap.roadmap.exception.RoadmapNodeNotFoundException;
import com.indianroadmap.roadmap.exception.RoadmapNotFoundException;
import com.indianroadmap.roadmap.mapper.RoadmapMapper;
import com.indianroadmap.roadmap.repository.RoadmapRepository;
import com.indianroadmap.roadmap.validation.RoadmapStructureValidator;
import com.indianroadmap.roadmap.validation.RoadmapValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapMapper roadmapMapper;
    private final DestinationClient destinationClient;
    private final RoadmapValidator roadmapValidator;
    private final RoadmapStructureValidator structureValidator;
    private final Clock clock;
    private final RoadmapProperties roadmapProperties;

    @org.springframework.beans.factory.annotation.Autowired
    public RoadmapServiceImpl(RoadmapRepository roadmapRepository, RoadmapMapper roadmapMapper,
                              DestinationClient destinationClient, RoadmapValidator roadmapValidator,
                              RoadmapStructureValidator structureValidator, Clock clock,
                              RoadmapProperties roadmapProperties) {
        this.roadmapRepository = roadmapRepository;
        this.roadmapMapper = roadmapMapper;
        this.destinationClient = destinationClient;
        this.roadmapValidator = roadmapValidator;
        this.structureValidator = structureValidator;
        this.clock = clock;
        this.roadmapProperties = roadmapProperties;
    }

    public RoadmapServiceImpl(RoadmapRepository roadmapRepository, RoadmapMapper roadmapMapper,
                              DestinationClient destinationClient, RoadmapValidator roadmapValidator,
                              RoadmapStructureValidator structureValidator, Clock clock) {
        this(roadmapRepository, roadmapMapper, destinationClient, roadmapValidator, structureValidator, clock,
            new RoadmapProperties(50, 200));
    }

    @Override
    public RoadmapResponse createRoadmap(CreateRoadmapRequest request) {
        roadmapValidator.validateCreateRequest(request);
        String normalizedSlug = roadmapValidator.normalizeSlug(request.slug());
        if (roadmapRepository.existsBySlug(normalizedSlug)) {
            throw new DuplicateRoadmapException(normalizedSlug);
        }

        CreateRoadmapRequest normalizedRequest = new CreateRoadmapRequest(
            normalizedSlug,
            request.name(),
            request.description(),
            request.status() == null ? RoadmapStatus.DRAFT : request.status()
        );
        RoadmapDocument document = roadmapMapper.mapToDocument(normalizedRequest, clock);
        document.setRouteSummary(calculateRouteSummary(document));
        RoadmapDocument saved = roadmapRepository.save(document);
        return roadmapMapper.mapToResponse(saved);
    }

    @Override
    public Page<RoadmapSummaryResponse> getRoadmaps(RoadmapStatus status, Pageable pageable) {
        Page<RoadmapDocument> page = status == null
            ? roadmapRepository.findAll(pageable)
            : roadmapRepository.findByStatus(status, pageable);
        return page.map(roadmapMapper::mapToSummaryResponse);
    }

    @Override
    public RoadmapResponse getRoadmap(String id) {
        return roadmapMapper.mapToResponse(getRoadmapDocument(id));
    }

    @Override
    public RoadmapResponse getRoadmapBySlug(String slug) {
        String normalizedSlug = roadmapValidator.normalizeSlug(slug);
        RoadmapDocument document = roadmapRepository.findBySlug(normalizedSlug)
            .orElseThrow(() -> new RoadmapNotFoundException(normalizedSlug));
        return roadmapMapper.mapToResponse(document);
    }

    @Override
    public RoadmapResponse updateRoadmap(String id, UpdateRoadmapRequest request) {
        RoadmapDocument roadmap = getRoadmapDocument(id);
        roadmap.setName(request.name().trim());
        roadmap.setDescription(request.description());
        roadmap.setUpdatedAt(Instant.now(clock));
        return roadmapMapper.mapToResponse(roadmapRepository.save(roadmap));
    }

    @Override
    public void deleteRoadmap(String id) {
        RoadmapDocument roadmap = getRoadmapDocument(id);
        roadmapRepository.delete(roadmap);
    }

    @Override
    public RoadmapResponse addNode(String roadmapId, AddRoadmapNodeRequest request) {
        RoadmapDocument roadmap = getRoadmapDocument(roadmapId);
        ensureNodeCapacity(roadmap);

        var destination = destinationClient.getDestination(request.destinationId());
        RoadmapNodeDocument node = new RoadmapNodeDocument(
            UUID.randomUUID().toString(),
            request.destinationId(),
            request.sequence(),
            request.label(),
            request.role(),
            destination.elevationMeters()
        );

        List<RoadmapNodeDocument> nodes = new ArrayList<>(roadmap.getNodes());
        int insertIndex = Math.max(0, Math.min(request.sequence() - 1, nodes.size()));
        nodes.add(insertIndex, node);
        resequence(nodes);
        roadmap.setNodes(nodes);
        structureValidator.validateNodeSequences(roadmap.getNodes());
        return saveAndMapWithSummary(roadmap);
    }

    @Override
    public List<RoadmapNodeResponse> getNodes(String roadmapId) {
        RoadmapDocument roadmap = getRoadmapDocument(roadmapId);
        return List.copyOf(roadmap.getNodes().stream()
            .sorted(Comparator.comparingInt(RoadmapNodeDocument::getSequence))
            .map(roadmapMapper::mapNodeToResponse)
            .toList());
    }

    @Override
    public RoadmapNodeResponse getNode(String roadmapId, String nodeId) {
        RoadmapDocument roadmap = getRoadmapDocument(roadmapId);
        return roadmapMapper.mapNodeToResponse(findNode(roadmap, nodeId));
    }

    @Override
    public RoadmapResponse updateNode(String roadmapId, String nodeId, UpdateRoadmapNodeRequest request) {
        RoadmapDocument roadmap = getRoadmapDocument(roadmapId);
        List<RoadmapNodeDocument> nodes = new ArrayList<>(roadmap.getNodes().stream()
            .sorted(Comparator.comparingInt(RoadmapNodeDocument::getSequence))
            .toList());

        RoadmapNodeDocument target = nodes.stream()
            .filter(node -> node.getNodeId().equals(nodeId))
            .findFirst()
            .orElseThrow(() -> new RoadmapNodeNotFoundException(nodeId));

        nodes.remove(target);
        int insertIndex = Math.max(0, Math.min(request.sequence() - 1, nodes.size()));
        target.setLabel(request.label());
        target.setRole(request.role());
        nodes.add(insertIndex, target);
        resequence(nodes);
        roadmap.setNodes(nodes);
        structureValidator.validateNodeSequences(roadmap.getNodes());
        return saveAndMapWithSummary(roadmap);
    }

    @Override
    public RoadmapResponse removeNode(String roadmapId, String nodeId) {
        RoadmapDocument roadmap = getRoadmapDocument(roadmapId);
        RoadmapNodeDocument node = findNode(roadmap, nodeId);
        roadmap.getNodes().remove(node);
        roadmap.getEdges().removeIf(edge -> edge.getFromNodeId().equals(nodeId) || edge.getToNodeId().equals(nodeId));
        resequence(roadmap.getNodes());
        return saveAndMapWithSummary(roadmap);
    }

    @Override
    public RoadmapResponse reorderNodes(String roadmapId, ReorderRoadmapNodesRequest request) {
        RoadmapDocument roadmap = getRoadmapDocument(roadmapId);
        List<RoadmapNodeDocument> existingNodes = new ArrayList<>(roadmap.getNodes());
        if (existingNodes.size() != request.nodeIds().size()) {
            throw new InvalidRoadmapException("All roadmap nodes must be included exactly once when reordering");
        }
        Set<String> uniqueIds = new HashSet<>(request.nodeIds());
        if (uniqueIds.size() != request.nodeIds().size()) {
            throw new InvalidRoadmapException("Duplicate node identifiers are not allowed in reorder request");
        }

        Map<String, RoadmapNodeDocument> nodeMap = existingNodes.stream()
            .collect(Collectors.toMap(RoadmapNodeDocument::getNodeId, node -> node));
        if (!nodeMap.keySet().equals(uniqueIds)) {
            throw new InvalidRoadmapException("Reorder request must contain every roadmap node exactly once");
        }

        List<RoadmapNodeDocument> reordered = new ArrayList<>();
        for (String nodeId : request.nodeIds()) {
            reordered.add(nodeMap.get(nodeId));
        }
        resequence(reordered);
        roadmap.setNodes(reordered);
        structureValidator.validateNodeSequences(roadmap.getNodes());
        return saveAndMapWithSummary(roadmap);
    }

    @Override
    public RoadmapResponse addEdge(String roadmapId, AddRoadmapEdgeRequest request) {
        RoadmapDocument roadmap = getRoadmapDocument(roadmapId);
        ensureEdgeCapacity(roadmap);
        structureValidator.validateEdge(roadmap, request);

        RoadmapEdgeDocument edge = new RoadmapEdgeDocument(
            UUID.randomUUID().toString(),
            request.fromNodeId(),
            request.toNodeId(),
            request.distanceKm(),
            request.estimatedTravelTimeMinutes(),
            request.roadType(),
            request.difficulty()
        );
        roadmap.getEdges().add(edge);
        return saveAndMapWithSummary(roadmap);
    }

    @Override
    public List<RoadmapEdgeResponse> getEdges(String roadmapId) {
        RoadmapDocument roadmap = getRoadmapDocument(roadmapId);
        return List.copyOf(roadmap.getEdges().stream().map(roadmapMapper::mapEdgeToResponse).toList());
    }

    @Override
    public RoadmapResponse updateEdge(String roadmapId, String edgeId, UpdateRoadmapEdgeRequest request) {
        RoadmapDocument roadmap = getRoadmapDocument(roadmapId);
        RoadmapEdgeDocument edge = findEdge(roadmap, edgeId);
        edge.setDistanceKm(request.distanceKm());
        edge.setEstimatedTravelTimeMinutes(request.estimatedTravelTimeMinutes());
        edge.setRoadType(request.roadType());
        edge.setDifficulty(request.difficulty());
        return saveAndMapWithSummary(roadmap);
    }

    @Override
    public RoadmapResponse removeEdge(String roadmapId, String edgeId) {
        RoadmapDocument roadmap = getRoadmapDocument(roadmapId);
        RoadmapEdgeDocument edge = findEdge(roadmap, edgeId);
        roadmap.getEdges().remove(edge);
        return saveAndMapWithSummary(roadmap);
    }

    @Override
    public RoadmapResponse publishRoadmap(String id) {
        RoadmapDocument roadmap = getRoadmapDocument(id);
        roadmapValidator.validateStatusTransition(roadmap.getStatus(), RoadmapStatus.PUBLISHED);
        structureValidator.validateForPublishing(roadmap);
        roadmap.setStatus(RoadmapStatus.PUBLISHED);
        return saveAndMapWithSummary(roadmap);
    }

    @Override
    public RoadmapResponse archiveRoadmap(String id) {
        RoadmapDocument roadmap = getRoadmapDocument(id);
        roadmapValidator.validateStatusTransition(roadmap.getStatus(), RoadmapStatus.ARCHIVED);
        roadmap.setStatus(RoadmapStatus.ARCHIVED);
        return saveAndMapWithSummary(roadmap);
    }

    RouteSummaryDocument calculateRouteSummary(RoadmapDocument roadmap) {
        List<RoadmapNodeDocument> nodes = roadmap.getNodes();
        List<RoadmapEdgeDocument> edges = roadmap.getEdges();

        double totalDistanceKm = edges.stream().mapToDouble(RoadmapEdgeDocument::getDistanceKm).sum();
        int totalTravelTimeMinutes = edges.stream().mapToInt(RoadmapEdgeDocument::getEstimatedTravelTimeMinutes).sum();
        int highestElevation = nodes.stream().mapToInt(RoadmapNodeDocument::getElevationMeters).max().orElse(0);
        int lowestElevation = nodes.isEmpty() ? 0 : nodes.stream().mapToInt(RoadmapNodeDocument::getElevationMeters).min().orElse(0);

        List<RoadmapNodeDocument> sorted = nodes.stream()
            .sorted(Comparator.comparingInt(RoadmapNodeDocument::getSequence))
            .toList();
        int elevationGain = 0;
        for (int i = 1; i < sorted.size(); i++) {
            int diff = sorted.get(i).getElevationMeters() - sorted.get(i - 1).getElevationMeters();
            if (diff > 0) {
                elevationGain += diff;
            }
        }

        RouteSummaryDocument summary = new RouteSummaryDocument();
        summary.setTotalDistanceKm(totalDistanceKm);
        summary.setTotalTravelTimeMinutes(totalTravelTimeMinutes);
        summary.setHighestElevationMeters(highestElevation);
        summary.setLowestElevationMeters(lowestElevation);
        summary.setElevationGainMeters(elevationGain);
        summary.setNodeCount(nodes.size());
        summary.setEdgeCount(edges.size());
        return summary;
    }

    private RoadmapResponse saveAndMapWithSummary(RoadmapDocument roadmap) {
        roadmap.setRouteSummary(calculateRouteSummary(roadmap));
        roadmap.setUpdatedAt(Instant.now(clock));
        RoadmapDocument saved = roadmapRepository.save(roadmap);
        return roadmapMapper.mapToResponse(saved);
    }

    private RoadmapDocument getRoadmapDocument(String roadmapId) {
        return roadmapRepository.findById(roadmapId)
            .orElseThrow(() -> new RoadmapNotFoundException(roadmapId));
    }

    private RoadmapNodeDocument findNode(RoadmapDocument roadmap, String nodeId) {
        return roadmap.getNodes().stream()
            .filter(node -> node.getNodeId().equals(nodeId))
            .findFirst()
            .orElseThrow(() -> new RoadmapNodeNotFoundException(nodeId));
    }

    private RoadmapEdgeDocument findEdge(RoadmapDocument roadmap, String edgeId) {
        return roadmap.getEdges().stream()
            .filter(edge -> edge.getEdgeId().equals(edgeId))
            .findFirst()
            .orElseThrow(() -> new InvalidRoadmapEdgeException("Roadmap edge not found: " + edgeId));
    }

    private void ensureNodeCapacity(RoadmapDocument roadmap) {
        if (roadmap.getNodes().size() >= roadmapProperties.maxNodesPerRoadmap()) {
            throw new InvalidRoadmapException("Roadmap has reached the maximum allowed nodes");
        }
    }

    private void ensureEdgeCapacity(RoadmapDocument roadmap) {
        if (roadmap.getEdges().size() >= roadmapProperties.maxEdgesPerRoadmap()) {
            throw new InvalidRoadmapException("Roadmap has reached the maximum allowed edges");
        }
    }

    private void resequence(List<RoadmapNodeDocument> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).setSequence(i + 1);
        }
    }
}
