package com.indianroadmap.destination.document;

import org.springframework.data.mongodb.core.mapping.Field;

public record DestinationName(
        @Field("default") String defaultName,
        @Field("local") String localName
) {}
