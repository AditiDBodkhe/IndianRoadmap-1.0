package com.indianroadmap.recommendation.dto.response;

public record ApiResponse<T>(boolean success, T data, ErrorDetail error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(ErrorDetail errorDetail) {
        return new ApiResponse<>(false, null, errorDetail);
    }
}
