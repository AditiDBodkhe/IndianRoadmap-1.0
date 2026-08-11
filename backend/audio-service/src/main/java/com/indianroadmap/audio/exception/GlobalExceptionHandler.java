package com.indianroadmap.audio.exception;

import com.indianroadmap.audio.dto.response.ErrorDetail;
import com.indianroadmap.audio.dto.response.ErrorResponse;
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

    @ExceptionHandler(AudioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAudioNotFound(AudioNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "AUDIO_NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(AudioAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAudioAlreadyExists(AudioAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, "AUDIO_ALREADY_EXISTS", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(AudioGenerationException.class)
    public ResponseEntity<ErrorResponse> handleAudioGeneration(AudioGenerationException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "AUDIO_GENERATION_ERROR", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(StoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStoryNotFound(StoryNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "STORY_NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(StoryServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleStoryServiceUnavailable(StoryServiceUnavailableException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "STORY_SERVICE_UNAVAILABLE", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(UnsupportedLanguageException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedLanguage(UnsupportedLanguageException ex) {
        return build(HttpStatus.BAD_REQUEST, "LANGUAGE_NOT_SUPPORTED", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(UnsupportedAudioFormatException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedFormat(UnsupportedAudioFormatException ex) {
        return build(HttpStatus.BAD_REQUEST, "UNSUPPORTED_AUDIO_FORMAT", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(TtsProviderException.class)
    public ResponseEntity<ErrorResponse> handleTtsProvider(TtsProviderException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "TTS_PROVIDER_ERROR", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fields.put(fieldError.getField(),
                    fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage());
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

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                 Map<String, String> fields) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(false, ErrorDetail.withFields(code, message, fields)));
    }
}
