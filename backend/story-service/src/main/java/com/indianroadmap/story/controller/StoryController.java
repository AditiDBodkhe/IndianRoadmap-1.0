package com.indianroadmap.story.controller;

import com.indianroadmap.story.document.StoryStatus;
import com.indianroadmap.story.document.StoryType;
import com.indianroadmap.story.dto.request.AddChapterRequest;
import com.indianroadmap.story.dto.request.AddSectionRequest;
import com.indianroadmap.story.dto.request.CreateStoryRequest;
import com.indianroadmap.story.dto.request.UpdateChapterRequest;
import com.indianroadmap.story.dto.request.UpdateSectionRequest;
import com.indianroadmap.story.dto.request.UpdateStoryRequest;
import com.indianroadmap.story.dto.response.ApiResponse;
import com.indianroadmap.story.dto.response.PageResponse;
import com.indianroadmap.story.dto.response.StoryChapterResponse;
import com.indianroadmap.story.dto.response.StoryResponse;
import com.indianroadmap.story.dto.response.StorySectionResponse;
import com.indianroadmap.story.dto.response.StorySummaryResponse;
import com.indianroadmap.story.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/stories")
@Validated
@Tag(name = "Stories", description = "Story management API")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @Operation(summary = "Create a story")
    @PostMapping
    public ResponseEntity<ApiResponse<StoryResponse>> createStory(@Valid @RequestBody CreateStoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(storyService.createStory(request)));
    }

    @Operation(summary = "List stories")
    @GetMapping
    public ResponseEntity<PageResponse<StorySummaryResponse>> getStories(
        @RequestParam(required = false) StoryStatus status,
        @RequestParam(required = false) StoryType storyType,
        @RequestParam(required = false) String destinationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StorySummaryResponse> result = storyService.getStories(status, storyType, destinationId, pageable);
        return ResponseEntity.ok(PageResponse.of(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements()));
    }

    @Operation(summary = "Get story by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoryResponse>> getStory(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.getStory(id)));
    }

    @Operation(summary = "Get story by slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<StoryResponse>> getStoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.getStoryBySlug(slug)));
    }

    @Operation(summary = "Update a story")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StoryResponse>> updateStory(@PathVariable String id,
                                                                  @Valid @RequestBody UpdateStoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.updateStory(id, request)));
    }

    @Operation(summary = "Delete a story")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(@PathVariable String id) {
        storyService.deleteStory(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get stories by destination")
    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<ApiResponse<List<StorySummaryResponse>>> getStoriesByDestination(
        @PathVariable String destinationId,
        @RequestParam(required = false) StoryStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.getStoriesByDestination(destinationId, status)));
    }

    @Operation(summary = "Add chapter")
    @PostMapping("/{storyId}/chapters")
    public ResponseEntity<ApiResponse<StoryResponse>> addChapter(@PathVariable String storyId,
                                                                 @Valid @RequestBody AddChapterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(storyService.addChapter(storyId, request)));
    }

    @Operation(summary = "Get chapters")
    @GetMapping("/{storyId}/chapters")
    public ResponseEntity<ApiResponse<List<StoryChapterResponse>>> getChapters(@PathVariable String storyId) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.getChapters(storyId)));
    }

    @Operation(summary = "Get chapter")
    @GetMapping("/{storyId}/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<StoryChapterResponse>> getChapter(@PathVariable String storyId,
                                                                        @PathVariable String chapterId) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.getChapter(storyId, chapterId)));
    }

    @Operation(summary = "Update chapter")
    @PutMapping("/{storyId}/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<StoryResponse>> updateChapter(@PathVariable String storyId,
                                                                    @PathVariable String chapterId,
                                                                    @Valid @RequestBody UpdateChapterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.updateChapter(storyId, chapterId, request)));
    }

    @Operation(summary = "Delete chapter")
    @DeleteMapping("/{storyId}/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<StoryResponse>> deleteChapter(@PathVariable String storyId,
                                                                    @PathVariable String chapterId) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.deleteChapter(storyId, chapterId)));
    }

    @Operation(summary = "Add section")
    @PostMapping("/{storyId}/chapters/{chapterId}/sections")
    public ResponseEntity<ApiResponse<StoryResponse>> addSection(@PathVariable String storyId,
                                                                 @PathVariable String chapterId,
                                                                 @Valid @RequestBody AddSectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(storyService.addSection(storyId, chapterId, request)));
    }

    @Operation(summary = "Get sections")
    @GetMapping("/{storyId}/chapters/{chapterId}/sections")
    public ResponseEntity<ApiResponse<List<StorySectionResponse>>> getSections(@PathVariable String storyId,
                                                                               @PathVariable String chapterId) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.getSections(storyId, chapterId)));
    }

    @Operation(summary = "Update section")
    @PutMapping("/{storyId}/chapters/{chapterId}/sections/{sectionId}")
    public ResponseEntity<ApiResponse<StoryResponse>> updateSection(@PathVariable String storyId,
                                                                    @PathVariable String chapterId,
                                                                    @PathVariable String sectionId,
                                                                    @Valid @RequestBody UpdateSectionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.updateSection(storyId, chapterId, sectionId, request)));
    }

    @Operation(summary = "Delete section")
    @DeleteMapping("/{storyId}/chapters/{chapterId}/sections/{sectionId}")
    public ResponseEntity<ApiResponse<StoryResponse>> deleteSection(@PathVariable String storyId,
                                                                    @PathVariable String chapterId,
                                                                    @PathVariable String sectionId) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.deleteSection(storyId, chapterId, sectionId)));
    }

    @Operation(summary = "Submit story for review")
    @PostMapping("/{id}/review")
    public ResponseEntity<ApiResponse<StoryResponse>> submitForReview(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.submitForReview(id)));
    }

    @Operation(summary = "Publish story")
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<StoryResponse>> publishStory(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.publishStory(id)));
    }

    @Operation(summary = "Archive story")
    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<StoryResponse>> archiveStory(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(storyService.archiveStory(id)));
    }
}
