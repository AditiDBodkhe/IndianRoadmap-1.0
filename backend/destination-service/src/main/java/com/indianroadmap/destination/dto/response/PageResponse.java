package com.indianroadmap.destination.dto.response;

import java.util.List;

public record PageResponse<T>(boolean success, List<T> data, PageMeta meta) {
    public static <T> PageResponse<T> of(List<T> data, int page, int size, long totalElements) {
        return new PageResponse<>(true, data, PageMeta.of(page, size, totalElements));
    }
}
