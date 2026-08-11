package com.indianroadmap.destination.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "destinations")
public class DestinationDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;

    private DestinationName name;
    private String state;
    private String district;
    private String region;
    private String shortDescription;
    private String description;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint coordinates;

    private Elevation elevation;

    private List<DestinationCategory> categories;
    private List<Mood> moods;
    private List<Language> languages;

    private List<HistoricalHighlight> historicalHighlights;
    private CulturalInformation culturalInformation;
    private ArchitectureInformation architecture;
    private List<Attraction> attractions;
    private List<ImageReference> images;
    private List<SourceReference> sources;

    private boolean verified;
    private Instant lastVerifiedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public DestinationName getName() { return name; }
    public void setName(DestinationName name) { this.name = name; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public GeoJsonPoint getCoordinates() { return coordinates; }
    public void setCoordinates(GeoJsonPoint coordinates) { this.coordinates = coordinates; }
    public Elevation getElevation() { return elevation; }
    public void setElevation(Elevation elevation) { this.elevation = elevation; }
    public List<DestinationCategory> getCategories() { return categories != null ? categories : List.of(); }
    public void setCategories(List<DestinationCategory> categories) { this.categories = categories; }
    public List<Mood> getMoods() { return moods != null ? moods : List.of(); }
    public void setMoods(List<Mood> moods) { this.moods = moods; }
    public List<Language> getLanguages() { return languages != null ? languages : List.of(); }
    public void setLanguages(List<Language> languages) { this.languages = languages; }
    public List<HistoricalHighlight> getHistoricalHighlights() { return historicalHighlights != null ? historicalHighlights : List.of(); }
    public void setHistoricalHighlights(List<HistoricalHighlight> historicalHighlights) { this.historicalHighlights = historicalHighlights; }
    public CulturalInformation getCulturalInformation() { return culturalInformation; }
    public void setCulturalInformation(CulturalInformation culturalInformation) { this.culturalInformation = culturalInformation; }
    public ArchitectureInformation getArchitecture() { return architecture; }
    public void setArchitecture(ArchitectureInformation architecture) { this.architecture = architecture; }
    public List<Attraction> getAttractions() { return attractions != null ? attractions : List.of(); }
    public void setAttractions(List<Attraction> attractions) { this.attractions = attractions; }
    public List<ImageReference> getImages() { return images != null ? images : List.of(); }
    public void setImages(List<ImageReference> images) { this.images = images; }
    public List<SourceReference> getSources() { return sources != null ? sources : List.of(); }
    public void setSources(List<SourceReference> sources) { this.sources = sources; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public Instant getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(Instant lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
