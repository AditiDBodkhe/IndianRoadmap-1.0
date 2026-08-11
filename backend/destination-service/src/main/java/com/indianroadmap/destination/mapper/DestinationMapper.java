package com.indianroadmap.destination.mapper;

import com.indianroadmap.destination.document.*;
import com.indianroadmap.destination.dto.request.*;
import com.indianroadmap.destination.dto.response.*;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DestinationMapper {

    public DestinationDocument toDocument(CreateDestinationRequest request) {
        var doc = new DestinationDocument();
        doc.setSlug(request.slug());
        doc.setName(toDestinationName(request.name()));
        doc.setState(request.state());
        doc.setDistrict(request.district());
        doc.setRegion(request.region());
        doc.setShortDescription(request.shortDescription());
        doc.setDescription(request.description());
        doc.setCoordinates(new GeoJsonPoint(request.longitude(), request.latitude()));
        doc.setElevation(new Elevation(request.elevationMeters(), request.elevationFeet()));
        doc.setCategories(safeList(request.categories()));
        doc.setMoods(safeList(request.moods()));
        doc.setLanguages(safeList(request.languages()));
        doc.setHistoricalHighlights(mapHistoricalHighlights(request.historicalHighlights()));
        doc.setCulturalInformation(toCulturalInformation(request.culturalInformation()));
        doc.setArchitecture(toArchitectureInformation(request.architecture()));
        doc.setAttractions(mapAttractions(request.attractions()));
        doc.setImages(mapImages(request.images()));
        doc.setSources(mapSources(request.sources()));
        doc.setVerified(false);
        return doc;
    }

    public DestinationResponse toResponse(DestinationDocument doc) {
        return new DestinationResponse(
                doc.getId(),
                doc.getSlug(),
                toNameResponse(doc.getName()),
                doc.getState(),
                doc.getDistrict(),
                doc.getRegion(),
                doc.getShortDescription(),
                doc.getDescription(),
                toCoordinatesResponse(doc.getCoordinates()),
                toElevationResponse(doc.getElevation()),
                List.copyOf(doc.getCategories()),
                List.copyOf(doc.getMoods()),
                List.copyOf(doc.getLanguages()),
                mapHistoricalHighlightResponses(doc.getHistoricalHighlights()),
                toCulturalInformationResponse(doc.getCulturalInformation()),
                toArchitectureResponse(doc.getArchitecture()),
                mapAttractionResponses(doc.getAttractions()),
                mapImageResponses(doc.getImages()),
                mapSourceResponses(doc.getSources()),
                doc.isVerified(),
                doc.getLastVerifiedAt(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    public DestinationSummaryResponse toSummary(DestinationDocument doc) {
        return new DestinationSummaryResponse(
                doc.getId(),
                doc.getSlug(),
                toNameResponse(doc.getName()),
                doc.getState(),
                doc.getRegion(),
                toCoordinatesResponse(doc.getCoordinates()),
                toElevationResponse(doc.getElevation()),
                List.copyOf(doc.getCategories()),
                List.copyOf(doc.getMoods())
        );
    }

    public void updateDocument(DestinationDocument doc, UpdateDestinationRequest request) {
        if (request.slug() != null) doc.setSlug(request.slug());
        if (request.name() != null) doc.setName(toDestinationName(request.name()));
        if (request.state() != null) doc.setState(request.state());
        if (request.district() != null) doc.setDistrict(request.district());
        if (request.region() != null) doc.setRegion(request.region());
        if (request.shortDescription() != null) doc.setShortDescription(request.shortDescription());
        if (request.description() != null) doc.setDescription(request.description());
        if (request.latitude() != null && request.longitude() != null) {
            doc.setCoordinates(new GeoJsonPoint(request.longitude(), request.latitude()));
        }
        if (request.elevationMeters() != null && request.elevationFeet() != null) {
            doc.setElevation(new Elevation(request.elevationMeters(), request.elevationFeet()));
        } else if (request.elevationMeters() != null) {
            int feet = doc.getElevation() != null ? doc.getElevation().feet() : 0;
            doc.setElevation(new Elevation(request.elevationMeters(), feet));
        } else if (request.elevationFeet() != null) {
            int meters = doc.getElevation() != null ? doc.getElevation().meters() : 0;
            doc.setElevation(new Elevation(meters, request.elevationFeet()));
        }
        if (request.categories() != null) doc.setCategories(safeList(request.categories()));
        if (request.moods() != null) doc.setMoods(safeList(request.moods()));
        if (request.languages() != null) doc.setLanguages(safeList(request.languages()));
        if (request.historicalHighlights() != null) doc.setHistoricalHighlights(mapHistoricalHighlights(request.historicalHighlights()));
        if (request.culturalInformation() != null) doc.setCulturalInformation(toCulturalInformation(request.culturalInformation()));
        if (request.architecture() != null) doc.setArchitecture(toArchitectureInformation(request.architecture()));
        if (request.attractions() != null) doc.setAttractions(mapAttractions(request.attractions()));
        if (request.images() != null) doc.setImages(mapImages(request.images()));
        if (request.sources() != null) doc.setSources(mapSources(request.sources()));
    }

    private DestinationName toDestinationName(DestinationNameRequest req) {
        if (req == null) return null;
        return new DestinationName(req.defaultName(), req.localName());
    }

    private DestinationNameResponse toNameResponse(DestinationName name) {
        if (name == null) return null;
        return new DestinationNameResponse(name.defaultName(), name.localName());
    }

    private ElevationResponse toElevationResponse(Elevation e) {
        if (e == null) return null;
        return new ElevationResponse(e.meters(), e.feet());
    }

    private CoordinatesResponse toCoordinatesResponse(GeoJsonPoint p) {
        if (p == null) return null;
        return new CoordinatesResponse(p.getY(), p.getX());
    }

    private CulturalInformation toCulturalInformation(CulturalInformationRequest req) {
        if (req == null) return null;
        return new CulturalInformation(req.traditions(), req.cuisine(), safeList(req.festivals()), req.attire(), req.notes());
    }

    private CulturalInformationResponse toCulturalInformationResponse(CulturalInformation c) {
        if (c == null) return null;
        return new CulturalInformationResponse(c.traditions(), c.cuisine(), safeList(c.festivals()), c.attire(), c.notes());
    }

    private ArchitectureInformation toArchitectureInformation(ArchitectureRequest req) {
        if (req == null) return null;
        return new ArchitectureInformation(req.style(), req.materials(), req.period(), req.notableFeatures());
    }

    private ArchitectureResponse toArchitectureResponse(ArchitectureInformation a) {
        if (a == null) return null;
        return new ArchitectureResponse(a.style(), a.materials(), a.period(), a.notableFeatures());
    }

    private List<HistoricalHighlight> mapHistoricalHighlights(List<HistoricalHighlightRequest> reqs) {
        if (reqs == null) return List.of();
        return reqs.stream()
                .map(r -> new HistoricalHighlight(r.era(), r.title(), r.description()))
                .toList();
    }

    private List<HistoricalHighlightResponse> mapHistoricalHighlightResponses(List<HistoricalHighlight> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(h -> new HistoricalHighlightResponse(h.era(), h.title(), h.description()))
                .toList();
    }

    private List<Attraction> mapAttractions(List<AttractionRequest> reqs) {
        if (reqs == null) return List.of();
        return reqs.stream()
                .map(r -> new Attraction(r.name(), r.type(), r.description()))
                .toList();
    }

    private List<AttractionResponse> mapAttractionResponses(List<Attraction> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(a -> new AttractionResponse(a.name(), a.type(), a.description()))
                .toList();
    }

    private List<ImageReference> mapImages(List<ImageReferenceRequest> reqs) {
        if (reqs == null) return List.of();
        return reqs.stream()
                .map(r -> new ImageReference(r.url(), r.caption(), r.photographer(), r.source()))
                .toList();
    }

    private List<ImageReferenceResponse> mapImageResponses(List<ImageReference> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(i -> new ImageReferenceResponse(i.url(), i.caption(), i.photographer(), i.source()))
                .toList();
    }

    private List<SourceReference> mapSources(List<SourceReferenceRequest> reqs) {
        if (reqs == null) return List.of();
        return reqs.stream()
                .map(r -> new SourceReference(r.title(), r.publisher(), r.url(), null))
                .toList();
    }

    private List<SourceReferenceResponse> mapSourceResponses(List<SourceReference> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(s -> new SourceReferenceResponse(s.title(), s.publisher(), s.url(), s.verifiedAt()))
                .toList();
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : List.copyOf(list);
    }
}
