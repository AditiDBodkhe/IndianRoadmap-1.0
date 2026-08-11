package com.indianroadmap.recommendation.dto.response;

import java.util.Map;

public record ErrorDetail(String code, String message, Map<String, String> fields) {

    public static ErrorDetail of(String code, String message) {
        return new ErrorDetail(code, message, Map.of());
    }

    public static ErrorDetail withFields(String code, String message, Map<String, String> fields) {
        return new ErrorDetail(code, message, Map.copyOf(fields));
    }
}
