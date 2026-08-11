package com.indianroadmap.destination.dto.response;
import java.time.Instant;
public record SourceReferenceResponse(String title, String publisher, String url, Instant verifiedAt) {}
