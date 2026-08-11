package com.indianroadmap.destination.service

import com.indianroadmap.destination.document.*
import com.indianroadmap.destination.dto.request.*
import com.indianroadmap.destination.dto.response.*
import com.indianroadmap.destination.exception.*
import com.indianroadmap.destination.mapper.DestinationMapper
import com.indianroadmap.destination.repository.DestinationRepository
import com.indianroadmap.destination.validation.DestinationValidator
import com.indianroadmap.destination.validation.SlugNormalizer
import org.springframework.data.mongodb.core.MongoTemplate
import spock.lang.Specification
import spock.lang.Title

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@Title("DestinationService — business logic specification")
class DestinationServiceSpec extends Specification {

    DestinationRepository repository = Mock()
    MongoTemplate mongoTemplate = Mock()
    DestinationMapper mapper = Mock()
    SlugNormalizer slugNormalizer = Mock()
    DestinationValidator validator = Mock()
    Clock clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC)

    DestinationService service = new DestinationServiceImpl(
            repository, mongoTemplate, mapper, slugNormalizer, validator, clock)

    def "creates a destination successfully"() {
        given:
        def request = Stub(CreateDestinationRequest) {
            slug() >> "chhitkul"
            latitude() >> 31.35
            longitude() >> 78.44
            elevationMeters() >> 3450
        }
        def doc = new DestinationDocument()
        doc.setSlug("chhitkul")
        def savedDoc = new DestinationDocument()
        savedDoc.setSlug("chhitkul")
        def response = new DestinationResponse("id1", "chhitkul", null, "HP", null, "Kinnaur", null, null, null, null, [], [], [], [], null, null, [], [], [], false, null, Instant.parse("2024-01-01T00:00:00Z"), Instant.parse("2024-01-01T00:00:00Z"))

        slugNormalizer.normalize("chhitkul") >> "chhitkul"
        repository.existsBySlug("chhitkul") >> false
        mapper.toDocument(request) >> doc
        mapper.toResponse(savedDoc) >> response

        when:
        def result = service.create(request)

        then:
        result == response
        1 * validator.validateCoordinates(31.35, 78.44)
        1 * validator.validateElevation(3450)
        1 * repository.save(doc) >> savedDoc
    }

    def "rejects duplicate slug on create"() {
        given:
        def request = Stub(CreateDestinationRequest) {
            slug() >> "chhitkul"
            latitude() >> 31.35
            longitude() >> 78.44
            elevationMeters() >> 3450
        }
        slugNormalizer.normalize("chhitkul") >> "chhitkul"
        repository.existsBySlug("chhitkul") >> true

        when:
        service.create(request)

        then:
        thrown(DuplicateDestinationException)
        0 * repository.save(_)
    }

    def "throws DestinationNotFoundException when findById gets missing id"() {
        given:
        repository.findById("missing-id") >> Optional.empty()

        when:
        service.findById("missing-id")

        then:
        thrown(DestinationNotFoundException)
    }

    def "findById returns response for existing destination"() {
        given:
        def doc = new DestinationDocument()
        doc.setId("id1")
        def response = Stub(DestinationResponse)
        
        repository.findById("id1") >> Optional.of(doc)
        mapper.toResponse(doc) >> response

        when:
        def result = service.findById("id1")

        then:
        result == response
    }

    def "findBySlug normalizes slug before querying"() {
        given:
        def doc = new DestinationDocument()
        doc.setSlug("chhitkul")
        def response = Stub(DestinationResponse)

        slugNormalizer.normalize("Chhitkul") >> "chhitkul"
        repository.findBySlug("chhitkul") >> Optional.of(doc)
        mapper.toResponse(doc) >> response

        when:
        def result = service.findBySlug("Chhitkul")

        then:
        result == response
    }

    def "delete throws DestinationNotFoundException for missing destination"() {
        given:
        repository.findById("missing") >> Optional.empty()

        when:
        service.delete("missing")

        then:
        thrown(DestinationNotFoundException)
    }

    def "delete removes existing destination"() {
        given:
        def doc = new DestinationDocument()
        doc.setId("id1")
        repository.findById("id1") >> Optional.of(doc)

        when:
        service.delete("id1")

        then:
        1 * repository.delete(doc)
    }

    def "update throws DestinationNotFoundException for missing destination"() {
        given:
        def request = Stub(UpdateDestinationRequest) {
            slug() >> null
        }
        repository.findById("missing") >> Optional.empty()

        when:
        service.update("missing", request)

        then:
        thrown(DestinationNotFoundException)
    }
}
