package com.indianroadmap.destination.service;

import com.indianroadmap.destination.document.DestinationCategory;
import com.indianroadmap.destination.document.DestinationDocument;
import com.indianroadmap.destination.document.Mood;
import com.indianroadmap.destination.dto.request.CreateDestinationRequest;
import com.indianroadmap.destination.dto.request.UpdateDestinationRequest;
import com.indianroadmap.destination.dto.response.DestinationResponse;
import com.indianroadmap.destination.dto.response.DestinationSummaryResponse;
import com.indianroadmap.destination.dto.response.PageResponse;
import com.indianroadmap.destination.exception.DestinationNotFoundException;
import com.indianroadmap.destination.exception.DuplicateDestinationException;
import com.indianroadmap.destination.mapper.DestinationMapper;
import com.indianroadmap.destination.repository.DestinationRepository;
import com.indianroadmap.destination.validation.DestinationValidator;
import com.indianroadmap.destination.validation.SlugNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class DestinationServiceImpl implements DestinationService {

    private static final Logger log = LoggerFactory.getLogger(DestinationServiceImpl.class);

    private final DestinationRepository repository;
    private final MongoTemplate mongoTemplate;
    private final DestinationMapper mapper;
    private final SlugNormalizer slugNormalizer;
    private final DestinationValidator validator;
    private final Clock clock;

    public DestinationServiceImpl(
            DestinationRepository repository,
            MongoTemplate mongoTemplate,
            DestinationMapper mapper,
            SlugNormalizer slugNormalizer,
            DestinationValidator validator,
            Clock clock) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
        this.mapper = mapper;
        this.slugNormalizer = slugNormalizer;
        this.validator = validator;
        this.clock = clock;
    }

    @Override
    public DestinationResponse create(CreateDestinationRequest request) {
        String normalizedSlug = slugNormalizer.normalize(request.slug());
        log.info("Creating destination with slug: {}", normalizedSlug);

        if (repository.existsBySlug(normalizedSlug)) {
            throw new DuplicateDestinationException(normalizedSlug);
        }

        validator.validateCoordinates(request.latitude(), request.longitude());
        validator.validateElevation(request.elevationMeters());

        DestinationDocument doc = mapper.toDocument(request);
        doc.setSlug(normalizedSlug);
        Instant now = Instant.now(clock);
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);
        doc.setVerified(false);

        DestinationDocument saved = repository.save(doc);
        log.info("Created destination id={}, slug={}", saved.getId(), saved.getSlug());
        return mapper.toResponse(saved);
    }

    @Override
    public DestinationResponse findById(String id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new DestinationNotFoundException(id));
    }

    @Override
    public DestinationResponse findBySlug(String slug) {
        String normalizedSlug = slugNormalizer.normalize(slug);
        return repository.findBySlug(normalizedSlug)
                .map(mapper::toResponse)
                .orElseThrow(() -> new DestinationNotFoundException(normalizedSlug));
    }

    @Override
    public PageResponse<DestinationSummaryResponse> search(String state, String region,
            DestinationCategory category, Mood mood, int page, int size) {
        Criteria criteria = buildSearchCriteria(state, region, category, mood);

        Query countQuery = Query.query(criteria);
        long total = mongoTemplate.count(countQuery, DestinationDocument.class);

        Query pageQuery = Query.query(criteria).with(PageRequest.of(page, size));
        List<DestinationDocument> docs = mongoTemplate.find(pageQuery, DestinationDocument.class);

        List<DestinationSummaryResponse> summaries = docs.stream()
                .map(mapper::toSummary)
                .toList();

        return PageResponse.of(summaries, page, size, total);
    }

    @Override
    public DestinationResponse update(String id, UpdateDestinationRequest request) {
        DestinationDocument doc = repository.findById(id)
                .orElseThrow(() -> new DestinationNotFoundException(id));

        if (request.slug() != null) {
            String newSlug = slugNormalizer.normalize(request.slug());
            if (!newSlug.equals(doc.getSlug()) && repository.existsBySlugAndIdNot(newSlug, id)) {
                throw new DuplicateDestinationException(newSlug);
            }
        }

        if (request.latitude() != null && request.longitude() != null) {
            validator.validateCoordinates(request.latitude(), request.longitude());
        }
        if (request.elevationMeters() != null) {
            validator.validateElevation(request.elevationMeters());
        }

        mapper.updateDocument(doc, request);
        doc.setUpdatedAt(Instant.now(clock));

        DestinationDocument saved = repository.save(doc);
        log.info("Updated destination id={}", id);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(String id) {
        DestinationDocument doc = repository.findById(id)
                .orElseThrow(() -> new DestinationNotFoundException(id));
        repository.delete(doc);
        log.info("Deleted destination id={}", id);
    }

    @Override
    public List<DestinationSummaryResponse> findNearby(double latitude, double longitude, double radiusMeters) {
        validator.validateCoordinates(latitude, longitude);
        validator.validateNearbySearchRadius(radiusMeters);

        GeoJsonPoint point = new GeoJsonPoint(longitude, latitude);
        NearQuery nearQuery = NearQuery.near(point)
                .maxDistance(new Distance(radiusMeters / 1000.0, Metrics.KILOMETERS))
                .spherical(true);

        var results = mongoTemplate.geoNear(nearQuery, DestinationDocument.class);

        return results.getContent().stream()
                .map(GeoResult::getContent)
                .map(mapper::toSummary)
                .toList();
    }

    private Criteria buildSearchCriteria(String state, String region, DestinationCategory category, Mood mood) {
        List<Criteria> conditions = new ArrayList<>();
        if (state != null && !state.isBlank()) conditions.add(Criteria.where("state").is(state));
        if (region != null && !region.isBlank()) conditions.add(Criteria.where("region").is(region));
        if (category != null) conditions.add(Criteria.where("categories").in(category));
        if (mood != null) conditions.add(Criteria.where("moods").in(mood));

        if (conditions.isEmpty()) return new Criteria();
        return new Criteria().andOperator(conditions.toArray(new Criteria[0]));
    }
}
