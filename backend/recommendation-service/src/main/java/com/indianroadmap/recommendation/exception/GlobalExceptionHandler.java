package com.indianroadmap.recommendation.exception;

import com.indianroadmap.recommendation.dto.response.ApiResponse;
import com.indianroadmap.recommendation.dto.response.ErrorDetail;
import com.indianroadmap.recommendation.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecommendationProfileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProfileNotFound(RecommendationProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(new ErrorDetail(
                        "RECOMMENDATION_PROFILE_NOT_FOUND", ex.getMessage(), Map.of())));
    }

    @ExceptionHandler(DestinationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDestinationNotFound(DestinationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(new ErrorDetail(
                        "DESTINATION_NOT_FOUND", ex.getMessage(), Map.of())));
    }

    @ExceptionHandler(DestinationServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceUnavailable(DestinationServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(new ErrorDetail(
                        "DESTINATION_SERVICE_UNAVAILABLE", ex.getMessage(), Map.of())));
    }

    @ExceptionHandler(InvalidRecommendationRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequest(InvalidRecommendationRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(new ErrorDetail(
                        "INVALID_RECOMMENDATION_REQUEST", ex.getMessage(), Map.of())));
    }

    @ExceptionHandler(RecommendationException.class)
    public ResponseEntity<ApiResponse<Void>> handleRecommendationException(RecommendationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(new ErrorDetail(
                        "RECOMMENDATION_ERROR", ex.getMessage(), Map.of())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (a, b) -> a));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(new ErrorDetail(
                        "VALIDATION_ERROR", "Request validation failed", fieldErrors)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(new ErrorDetail(
                        "INTERNAL_ERROR", "An unexpected error occurred", Map.of())));
    }
}
