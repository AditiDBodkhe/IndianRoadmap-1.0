package com.indianroadmap.destination.document;

import java.time.Instant;

public record SourceReference(String title, String publisher, String url, Instant verifiedAt) {}
