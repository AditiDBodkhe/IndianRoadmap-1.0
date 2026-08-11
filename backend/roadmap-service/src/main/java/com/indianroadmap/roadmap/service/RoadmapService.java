package com.indianroadmap.roadmap.service;

import com.indianroadmap.roadmap.document.RoadmapStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoadmapService {
    RoadmapResponse createRoadmap(CreateRoadmapRequest request);
    Page<RoadmapSummaryResponse> getRoadmaps(RoadmapStatus status, Pageable pageable);
    RoadmapResponse getRoadmap(String id);
    RoadmapResponse getRoadmapBySlug(String slug);
    RoadmapResponse updateRoadmap(String id, UpdateRoadmapRequest request);
    void deleteRoadmap(String id);
    RoadmapResponse addNode(String roadmapId, AddRoadmapNodeRequest request);
    List<RoadmapNodeResponse> getNodes(String roadmapId);
    RoadmapNodeResponse getNode(String roadmapId, String nodeId);
    RoadmapResponse updateNode(String roadmapId, String nodeId, UpdateRoadmapNodeRequest request);
    RoadmapResponse removeNode(String roadmapId, String nodeId);
    RoadmapResponse reorderNodes(String roadmapId, ReorderRoadmapNodesRequest request);
    RoadmapResponse addEdge(String roadmapId, AddRoadmapEdgeRequest request);
    List<RoadmapEdgeResponse> getEdges(String roadmapId);
    RoadmapResponse updateEdge(String roadmapId, String edgeId, UpdateRoadmapEdgeRequest request);
    RoadmapResponse removeEdge(String roadmapId, String edgeId);
    RoadmapResponse publishRoadmap(String id);
    RoadmapResponse archiveRoadmap(String id);
}
