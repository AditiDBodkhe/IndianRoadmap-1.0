package com.indianroadmap.user.exception;

import com.indianroadmap.user.dto.response.ApiResponse;
import com.indianroadmap.user.dto.response.ErrorDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return response(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return response(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountLocked(AccountLockedException ex) {
        return response(HttpStatus.FORBIDDEN, "ACCOUNT_LOCKED", ex.getMessage());
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountDisabled(AccountDisabledException ex) {
        return response(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenInvalid(RefreshTokenInvalidException ex) {
        return response(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_INVALID", ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        return response(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException ex) {
        return response(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.badRequest().body(ApiResponse.error(
                ErrorDetail.of("VALIDATION_ERROR", "Request validation failed", details)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        String message = ex.getMessage() == null ? "Unexpected server error" : ex.getMessage();
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", message);
    }

    private ResponseEntity<ApiResponse<Void>> response(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(ErrorDetail.of(code, message)));
    }
}
