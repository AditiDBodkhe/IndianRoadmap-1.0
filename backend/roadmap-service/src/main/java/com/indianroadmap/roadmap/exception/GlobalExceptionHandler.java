package com.indianroadmap.roadmap.exception;

import com.indianroadmap.roadmap.dto.response.ErrorDetail;
import com.indianroadmap.roadmap.dto.response.ErrorResponse;
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

    @ExceptionHandler(RoadmapNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoadmapNotFound(RoadmapNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "ROADMAP_NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(RoadmapNodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoadmapNodeNotFound(RoadmapNodeNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "ROADMAP_NODE_NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DuplicateRoadmapException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRoadmap(DuplicateRoadmapException ex) {
        return build(HttpStatus.CONFLICT, "DUPLICATE_ROADMAP", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidRoadmapException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRoadmap(InvalidRoadmapException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_ROADMAP", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DestinationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDestinationNotFound(DestinationNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "DESTINATION_NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DestinationServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleDestinationServiceUnavailable(DestinationServiceUnavailableException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "DESTINATION_SERVICE_UNAVAILABLE", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidRoadmapEdgeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRoadmapEdge(InvalidRoadmapEdgeException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_ROADMAP_EDGE", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidRoadmapStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRoadmapStatus(InvalidRoadmapStatusException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_ROADMAP_STATUS", ex.getMessage(), Map.of());
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
            .body(new ErrorResponse(false, new ErrorDetail(code, message, fields)));
    }
}
