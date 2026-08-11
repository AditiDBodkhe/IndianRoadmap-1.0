package com.indianroadmap.destination.document;

import java.util.List;

public record CulturalInformation(
        String traditions, String cuisine, List<String> festivals, String attire, String notes
) {}
