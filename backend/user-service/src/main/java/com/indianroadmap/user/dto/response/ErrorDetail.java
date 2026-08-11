package com.indianroadmap.user.dto.response;

import java.util.List;

public record ErrorDetail(String code, String message, List<String> details) {

    public ErrorDetail {
        details = details == null ? List.of() : List.copyOf(details);
    }

    public static ErrorDetail of(String code, String message) {
        return new ErrorDetail(code, message, List.of());
    }

    public static ErrorDetail of(String code, String message, List<String> details) {
        return new ErrorDetail(code, message, details);
    }
}
