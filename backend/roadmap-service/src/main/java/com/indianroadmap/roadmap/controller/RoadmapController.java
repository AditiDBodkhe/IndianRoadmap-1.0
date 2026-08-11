package com.indianroadmap.roadmap.controller;

import com.indianroadmap.roadmap.document.RoadmapStatus;
import com.indianroadmap.roadmap.dto.request.AddRoadmapEdgeRequest;
import com.indianroadmap.roadmap.dto.request.AddRoadmapNodeRequest;
import com.indianroadmap.roadmap.dto.request.CreateRoadmapRequest;
import com.indianroadmap.roadmap.dto.request.ReorderRoadmapNodesRequest;
import com.indianroadmap.roadmap.dto.request.UpdateRoadmapEdgeRequest;
import com.indianroadmap.roadmap.dto.request.UpdateRoadmapNodeRequest;
import com.indianroadmap.roadmap.dto.request.UpdateRoadmapRequest;
import com.indianroadmap.roadmap.dto.response.ApiResponse;
import com.indianroadmap.roadmap.dto.response.PageMeta;
import com.indianroadmap.roadmap.dto.response.PagedApiResponse;
import com.indianroadmap.roadmap.dto.response.RoadmapEdgeResponse;
import com.indianroadmap.roadmap.dto.response.RoadmapNodeResponse;
import com.indianroadmap.roadmap.dto.response.RoadmapResponse;
import com.indianroadmap.roadmap.dto.response.RoadmapSummaryResponse;
import com.indianroadmap.roadmap.service.RoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roadmaps")
@Validated
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @Operation(summary = "Create roadmap")
    @PostMapping
    public ResponseEntity<ApiResponse<RoadmapResponse>> createRoadmap(@Valid @RequestBody CreateRoadmapRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, roadmapService.createRoadmap(request)));
    }

    @Operation(summary = "List roadmaps")
    @GetMapping
    public ResponseEntity<PagedApiResponse<RoadmapSummaryResponse>> getRoadmaps(
        @RequestParam(required = false) RoadmapStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RoadmapSummaryResponse> result = roadmapService.getRoadmaps(status, pageable);
        return ResponseEntity.ok(new PagedApiResponse<>(
            true,
            List.copyOf(result.getContent()),
            new PageMeta(result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages())
        ));
    }

    @Operation(summary = "Get roadmap by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoadmapResponse>> getRoadmap(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.getRoadmap(id)));
    }

    @Operation(summary = "Get roadmap by slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<RoadmapResponse>> getRoadmapBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.getRoadmapBySlug(slug)));
    }

    @Operation(summary = "Update roadmap")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoadmapResponse>> updateRoadmap(@PathVariable String id,
                                                                      @Valid @RequestBody UpdateRoadmapRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.updateRoadmap(id, request)));
    }

    @Operation(summary = "Delete roadmap")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoadmap(@PathVariable String id) {
        roadmapService.deleteRoadmap(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add roadmap node")
    @PostMapping("/{roadmapId}/nodes")
    public ResponseEntity<ApiResponse<RoadmapResponse>> addNode(@PathVariable String roadmapId,
                                                                @Valid @RequestBody AddRoadmapNodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, roadmapService.addNode(roadmapId, request)));
    }

    @Operation(summary = "Get roadmap nodes")
    @GetMapping("/{roadmapId}/nodes")
    public ResponseEntity<ApiResponse<List<RoadmapNodeResponse>>> getNodes(@PathVariable String roadmapId) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.getNodes(roadmapId)));
    }

    @Operation(summary = "Get roadmap node")
    @GetMapping("/{roadmapId}/nodes/{nodeId}")
    public ResponseEntity<ApiResponse<RoadmapNodeResponse>> getNode(@PathVariable String roadmapId, @PathVariable String nodeId) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.getNode(roadmapId, nodeId)));
    }

    @Operation(summary = "Update roadmap node")
    @PutMapping("/{roadmapId}/nodes/{nodeId}")
    public ResponseEntity<ApiResponse<RoadmapResponse>> updateNode(@PathVariable String roadmapId,
                                                                   @PathVariable String nodeId,
                                                                   @Valid @RequestBody UpdateRoadmapNodeRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.updateNode(roadmapId, nodeId, request)));
    }

    @Operation(summary = "Remove roadmap node")
    @DeleteMapping("/{roadmapId}/nodes/{nodeId}")
    public ResponseEntity<ApiResponse<RoadmapResponse>> removeNode(@PathVariable String roadmapId, @PathVariable String nodeId) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.removeNode(roadmapId, nodeId)));
    }

    @Operation(summary = "Reorder roadmap nodes")
    @PutMapping("/{roadmapId}/nodes/reorder")
    public ResponseEntity<ApiResponse<RoadmapResponse>> reorderNodes(@PathVariable String roadmapId,
                                                                     @Valid @RequestBody ReorderRoadmapNodesRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.reorderNodes(roadmapId, request)));
    }

    @Operation(summary = "Add roadmap edge")
    @PostMapping("/{roadmapId}/edges")
    public ResponseEntity<ApiResponse<RoadmapResponse>> addEdge(@PathVariable String roadmapId,
                                                                @Valid @RequestBody AddRoadmapEdgeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, roadmapService.addEdge(roadmapId, request)));
    }

    @Operation(summary = "Get roadmap edges")
    @GetMapping("/{roadmapId}/edges")
    public ResponseEntity<ApiResponse<List<RoadmapEdgeResponse>>> getEdges(@PathVariable String roadmapId) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.getEdges(roadmapId)));
    }

    @Operation(summary = "Update roadmap edge")
    @PutMapping("/{roadmapId}/edges/{edgeId}")
    public ResponseEntity<ApiResponse<RoadmapResponse>> updateEdge(@PathVariable String roadmapId,
                                                                   @PathVariable String edgeId,
                                                                   @Valid @RequestBody UpdateRoadmapEdgeRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.updateEdge(roadmapId, edgeId, request)));
    }

    @Operation(summary = "Remove roadmap edge")
    @DeleteMapping("/{roadmapId}/edges/{edgeId}")
    public ResponseEntity<ApiResponse<RoadmapResponse>> removeEdge(@PathVariable String roadmapId, @PathVariable String edgeId) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.removeEdge(roadmapId, edgeId)));
    }

    @Operation(summary = "Publish roadmap")
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<RoadmapResponse>> publishRoadmap(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.publishRoadmap(id)));
    }

    @Operation(summary = "Archive roadmap")
    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<RoadmapResponse>> archiveRoadmap(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponse<>(true, roadmapService.archiveRoadmap(id)));
    }
}
