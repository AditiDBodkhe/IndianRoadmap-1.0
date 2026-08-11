package com.indianroadmap.destination.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DestinationNameRequest(
        @NotBlank String defaultName,
        String localName
) {}
