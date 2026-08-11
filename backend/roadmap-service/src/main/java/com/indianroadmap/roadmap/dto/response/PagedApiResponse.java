package com.indianroadmap.roadmap.dto.response;

import java.util.List;

public record PagedApiResponse<T>(boolean success, List<T> data, PageMeta meta) {
}
