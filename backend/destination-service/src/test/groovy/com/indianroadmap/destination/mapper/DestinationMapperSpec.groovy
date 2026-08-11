package com.indianroadmap.destination.mapper

import com.indianroadmap.destination.document.*
import com.indianroadmap.destination.dto.request.*
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import spock.lang.Specification
import spock.lang.Title

import java.time.Instant

@Title("DestinationMapper — document to response and request to document mapping")
class DestinationMapperSpec extends Specification {

    DestinationMapper mapper = new DestinationMapper()

    def "maps DestinationDocument to DestinationResponse correctly"() {
        given:
        def doc = new DestinationDocument()
        doc.setId("abc123")
        doc.setSlug("chhitkul")
        doc.setName(new DestinationName("Chhitkul", "Chhitkul"))
        doc.setState("Himachal Pradesh")
        doc.setDistrict("Kinnaur")
        doc.setRegion("Kinnaur")
        doc.setShortDescription("A high-altitude Himalayan village")
        doc.setDescription("")
        doc.setCoordinates(new GeoJsonPoint(78.44, 31.35))
        doc.setElevation(new Elevation(3450, 11319))
        doc.setCategories(List.of(DestinationCategory.VILLAGE, DestinationCategory.HERITAGE))
        doc.setMoods(List.of(Mood.ADVENTURE, Mood.HERITAGE))
        doc.setLanguages(List.of(Language.ENGLISH, Language.HINDI))
        doc.setHistoricalHighlights([])
        doc.setAttractions([])
        doc.setImages([])
        doc.setSources([])
        doc.setVerified(false)
        def now = Instant.parse("2024-01-01T00:00:00Z")
        doc.setCreatedAt(now)
        doc.setUpdatedAt(now)

        when:
        def response = mapper.toResponse(doc)

        then:
        response.id() == "abc123"
        response.slug() == "chhitkul"
        response.name().defaultName() == "Chhitkul"
        response.state() == "Himachal Pradesh"
        response.coordinates().latitude() == 31.35
        response.coordinates().longitude() == 78.44
        response.elevation().meters() == 3450
        response.elevation().feet() == 11319
        response.categories() == [DestinationCategory.VILLAGE, DestinationCategory.HERITAGE]
        response.moods() == [Mood.ADVENTURE, Mood.HERITAGE]
        response.verified() == false
        response.createdAt() == now
    }

    def "maps DestinationDocument to DestinationSummaryResponse correctly"() {
        given:
        def doc = new DestinationDocument()
        doc.setId("xyz789")
        doc.setSlug("kaza")
        doc.setName(new DestinationName("Kaza", "Kaza"))
        doc.setState("Himachal Pradesh")
        doc.setRegion("Lahaul and Spiti")
        doc.setCoordinates(new GeoJsonPoint(78.07, 32.22))
        doc.setElevation(new Elevation(3800, 12467))
        doc.setCategories(List.of(DestinationCategory.CITY))
        doc.setMoods(List.of(Mood.ADVENTURE))

        when:
        def summary = mapper.toSummary(doc)

        then:
        summary.id() == "xyz789"
        summary.slug() == "kaza"
        summary.coordinates().latitude() == 32.22
        summary.coordinates().longitude() == 78.07
    }

    def "maps CreateDestinationRequest to DestinationDocument correctly"() {
        given:
        def nameReq = new DestinationNameRequest("Hanle", "Hanle")
        def request = new CreateDestinationRequest(
                "hanle",
                nameReq,
                "Ladakh",
                "Leh",
                "Leh",
                "Remote high-altitude village",
                "",
                32.77,
                78.96,
                4500,
                14764,
                [DestinationCategory.VILLAGE],
                [Mood.SOLITUDE],
                [Language.HINDI],
                [],
                null,
                null,
                [],
                [],
                []
        )

        when:
        def doc = mapper.toDocument(request)

        then:
        doc.getSlug() == "hanle"
        doc.getName().defaultName() == "Hanle"
        doc.getState() == "Ladakh"
        doc.getCoordinates().getX() == 78.96
        doc.getCoordinates().getY() == 32.77
        doc.getElevation().meters() == 4500
        doc.getCategories() == [DestinationCategory.VILLAGE]
    }
}
