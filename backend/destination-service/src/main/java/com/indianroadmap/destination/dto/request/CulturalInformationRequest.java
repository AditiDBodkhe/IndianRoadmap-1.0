package com.indianroadmap.destination.dto.request;

import java.util.List;

public record CulturalInformationRequest(
        String traditions, String cuisine, List<String> festivals, String attire, String notes
) {}
