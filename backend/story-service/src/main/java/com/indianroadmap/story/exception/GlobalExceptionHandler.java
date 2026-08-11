package com.indianroadmap.story.exception;

import com.indianroadmap.story.dto.response.ErrorDetail;
import com.indianroadmap.story.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStoryNotFound(StoryNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "STORY_NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(ChapterNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChapterNotFound(ChapterNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "CHAPTER_NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(SectionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSectionNotFound(SectionNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "SECTION_NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DuplicateStoryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateStory(DuplicateStoryException ex) {
        return build(HttpStatus.CONFLICT, "DUPLICATE_STORY", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DestinationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDestinationNotFound(DestinationNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "DESTINATION_NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidStoryException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStory(InvalidStoryException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_STORY", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidStoryStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStoryStatus(InvalidStoryStatusException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_STORY_STATUS", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidStoryStructureException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStoryStructure(InvalidStoryStructureException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_STORY_STRUCTURE", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DestinationServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleDestinationServiceUnavailable(DestinationServiceUnavailableException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "DESTINATION_SERVICE_UNAVAILABLE", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fields.put(fieldError.getField(), fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Malformed request body", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", Map.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, Map<String, String> fields) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(false, ErrorDetail.withFields(code, message, fields)));
    }
}
