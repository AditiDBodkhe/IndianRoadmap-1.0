package com.indianroadmap.audio.dto.response;

import java.util.List;

public record PageResponse<T>(boolean success, List<T> data, PageMeta meta) {

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(true, List.copyOf(items), new PageMeta(page, size, totalElements, totalPages));
    }

    public record PageMeta(int page, int size, long totalElements, int totalPages) {}
}
