package com.indianroadmap.destination.controller;

import com.indianroadmap.destination.document.DestinationCategory;
import com.indianroadmap.destination.document.Mood;
import com.indianroadmap.destination.dto.request.CreateDestinationRequest;
import com.indianroadmap.destination.dto.request.UpdateDestinationRequest;
import com.indianroadmap.destination.dto.response.*;
import com.indianroadmap.destination.service.DestinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/destinations")
@Tag(name = "Destinations", description = "Destination management API")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new destination")
    public ApiResponse<DestinationResponse> create(@Valid @RequestBody CreateDestinationRequest request) {
        return ApiResponse.ok(destinationService.create(request));
    }

    @GetMapping
    @Operation(summary = "Search and list destinations with optional filters and pagination")
    public PageResponse<DestinationSummaryResponse> search(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) DestinationCategory category,
            @RequestParam(required = false) Mood mood,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return destinationService.search(state, region, category, mood, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get destination by ID")
    public ApiResponse<DestinationResponse> getById(@PathVariable String id) {
        return ApiResponse.ok(destinationService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get destination by slug")
    public ApiResponse<DestinationResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(destinationService.findBySlug(slug));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find destinations near a coordinate")
    public ApiResponse<List<DestinationSummaryResponse>> findNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "50000") double radius) {
        return ApiResponse.ok(destinationService.findNearby(latitude, longitude, radius));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a destination")
    public ApiResponse<DestinationResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateDestinationRequest request) {
        return ApiResponse.ok(destinationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a destination")
    public void delete(@PathVariable String id) {
        destinationService.delete(id);
    }
}
