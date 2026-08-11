package com.indianroadmap.destination.validation;

import com.indianroadmap.destination.exception.InvalidDestinationException;
import org.springframework.stereotype.Component;

@Component
public class DestinationValidator {
    
    public void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new InvalidDestinationException(
                "Latitude must be between -90 and 90, got: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new InvalidDestinationException(
                "Longitude must be between -180 and 180, got: " + longitude);
        }
    }
    
    public void validateElevation(int elevationMeters) {
        if (elevationMeters < 0) {
            throw new InvalidDestinationException(
                "Elevation cannot be negative: " + elevationMeters);
        }
    }
    
    public void validateNearbySearchRadius(double radiusMeters) {
        if (radiusMeters <= 0) {
            throw new InvalidDestinationException(
                "Search radius must be positive: " + radiusMeters);
        }
        if (radiusMeters > 1_000_000) {
            throw new InvalidDestinationException(
                "Search radius too large (max 1,000,000 meters): " + radiusMeters);
        }
    }
}
