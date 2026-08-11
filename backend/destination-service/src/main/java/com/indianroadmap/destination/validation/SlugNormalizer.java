package com.indianroadmap.destination.validation;

import org.springframework.stereotype.Component;

@Component
public class SlugNormalizer {
    
    public String normalize(String input) {
        if (input == null) return "";
        return input.trim()
                .toLowerCase()
                .replaceAll("\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
    
    public boolean isValid(String slug) {
        if (slug == null || slug.isBlank()) return false;
        return slug.matches("^[a-z0-9]+(-[a-z0-9]+)*$");
    }
}
