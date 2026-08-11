package com.indianroadmap.audio.controller;

import com.indianroadmap.audio.document.AudioFormat;
import com.indianroadmap.audio.document.AudioLanguage;
import com.indianroadmap.audio.document.AudioStatus;
import com.indianroadmap.audio.dto.request.GenerateAudioRequest;
import com.indianroadmap.audio.dto.request.RegenerateAudioRequest;
import com.indianroadmap.audio.dto.response.ApiResponse;
import com.indianroadmap.audio.dto.response.AudioResponse;
import com.indianroadmap.audio.dto.response.PageResponse;
import com.indianroadmap.audio.exception.AudioNotFoundException;
import com.indianroadmap.audio.service.AudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audio")
@Tag(name = "Audio", description = "Audio generation and management")
public class AudioController {

    private final AudioService audioService;

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping
    @Operation(summary = "Generate audio for a story section")
    public ResponseEntity<ApiResponse<AudioResponse>> generateAudio(
            @Valid @RequestBody GenerateAudioRequest request) {
        AudioResponse response = audioService.generateAudio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{audioId}")
    @Operation(summary = "Get audio asset by ID")
    public ResponseEntity<ApiResponse<AudioResponse>> getAudio(@PathVariable String audioId) {
        return ResponseEntity.ok(ApiResponse.ok(audioService.getAudio(audioId)));
    }

    @DeleteMapping("/{audioId}")
    @Operation(summary = "Delete an audio asset")
    public ResponseEntity<Void> deleteAudio(@PathVariable String audioId) {
        audioService.deleteAudio(audioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{audioId}/regenerate")
    @Operation(summary = "Regenerate audio with updated voice or format")
    public ResponseEntity<ApiResponse<AudioResponse>> regenerateAudio(
            @PathVariable String audioId,
            @Valid @RequestBody RegenerateAudioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(audioService.regenerateAudio(audioId, request)));
    }

    @GetMapping("/{audioId}/content")
    @Operation(summary = "Download audio file content")
    public ResponseEntity<byte[]> getAudioContent(@PathVariable String audioId) {
        AudioResponse meta = audioService.getAudio(audioId);
        byte[] content = audioService.getAudioContent(audioId);
        AudioFormat format = meta.format();
        String contentType = format != null ? format.contentType() : "application/octet-stream";
        String filename = "audio-" + audioId + "." + (format != null ? format.extension() : "bin");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(content.length))
                .body(content);
    }

    @GetMapping("/story/{storyId}")
    @Operation(summary = "List audio assets for a story")
    public ResponseEntity<PageResponse<AudioResponse>> listByStory(
            @PathVariable String storyId,
            @RequestParam(required = false) AudioLanguage language,
            @RequestParam(required = false) AudioStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(audioService.listByStory(storyId, language, status, pageable));
    }

    @GetMapping("/section/{sectionId}")
    @Operation(summary = "List audio assets for a section")
    public ResponseEntity<PageResponse<AudioResponse>> listBySection(
            @PathVariable String sectionId,
            @RequestParam(required = false) AudioLanguage language,
            @RequestParam(required = false) AudioStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(audioService.listBySection(sectionId, language, status, pageable));
    }
}
