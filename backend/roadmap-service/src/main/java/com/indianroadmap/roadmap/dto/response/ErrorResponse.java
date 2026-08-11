package com.indianroadmap.roadmap.dto.response;

public record ErrorResponse(boolean success, ErrorDetail error) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(false, ErrorDetail.of(code, message));
    }
}
