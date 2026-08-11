package com.indianroadmap.story.initializer;

import com.indianroadmap.story.calculator.ReadingTimeCalculator;
import com.indianroadmap.story.client.DestinationClient;
import com.indianroadmap.story.client.DestinationSummary;
import com.indianroadmap.story.document.StoryChapterDocument;
import com.indianroadmap.story.document.StoryDifficulty;
import com.indianroadmap.story.document.StoryDocument;
import com.indianroadmap.story.document.StoryLanguage;
import com.indianroadmap.story.document.StorySectionDocument;
import com.indianroadmap.story.document.StorySourceDocument;
import com.indianroadmap.story.document.StoryStatus;
import com.indianroadmap.story.document.StoryType;
import com.indianroadmap.story.document.SourceType;
import com.indianroadmap.story.repository.StoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile({"local", "dev"})
public class StoryDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(StoryDataInitializer.class);

    private final StoryRepository storyRepository;
    private final ReadingTimeCalculator readingTimeCalculator;
    private final DestinationClient destinationClient;
    private final Clock clock;

    public StoryDataInitializer(StoryRepository storyRepository,
                                ReadingTimeCalculator readingTimeCalculator,
                                DestinationClient destinationClient,
                                Clock clock) {
        this.storyRepository = storyRepository;
        this.readingTimeCalculator = readingTimeCalculator;
        this.destinationClient = destinationClient;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (!waitForDestinationService()) {
            log.warn("Skipping story seeding because destination-service is not reachable during startup");
            return;
        }
        for (String slug : priorityStorySlugs()) {
            String storySlug = slug + "-story";
            if (storyRepository.existsBySlug(storySlug)) continue;

            try {
                DestinationSummary destination = resolveDestination(slug);
                StoryDocument story = buildStory(destination, storySlug);
                storyRepository.save(story);
                log.info("Seeded story: {}", storySlug);
            } catch (Exception ex) {
                log.warn("Skipping story seed for {}: {}", slug, ex.getMessage());
            }
        }
    }

    private boolean waitForDestinationService() {
        for (int attempt = 1; attempt <= 20; attempt++) {
            try {
                destinationClient.getDestinationBySlug("mumbai");
                return true;
            } catch (Exception ignored) {
                sleep(1200);
            }
        }
        return false;
    }

    private DestinationSummary resolveDestination(String slug) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                return destinationClient.getDestinationBySlug(slug);
            } catch (RuntimeException ex) {
                last = ex;
                sleep(700);
            }
        }
        if (last != null) throw last;
        throw new IllegalStateException("Unable to resolve destination " + slug);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private StoryDocument buildStory(DestinationSummary destination, String storySlug) {
        Instant now = Instant.now(clock);
        String destinationName = destination.name();

        StoryDocument story = new StoryDocument();
        story.setSlug(storySlug);
        story.setDestinationId(destination.id());
        story.setTitle("The Story of " + destinationName);
        story.setShortDescription("A chaptered narrative of place, memory, culture, and present-day life in " + destinationName + ".");
        story.setStoryType(inferType(destinationName));
        story.setStatus(StoryStatus.PUBLISHED);
        story.setDifficulty(StoryDifficulty.STANDARD);
        story.setAvailableLanguages(List.of(StoryLanguage.ENGLISH, StoryLanguage.HINDI));
        story.setChapters(chaptersFor(destinationName));
        story.setSources(List.of(
                new StorySourceDocument("State Tourism Compendium", "State Tourism Department", "https://www.incredibleindia.gov.in", SourceType.OFFICIAL, true, now),
                new StorySourceDocument("Archaeological and Heritage Notes", "ASI", "https://asi.nic.in", SourceType.ARCHIVE, true, now),
                new StorySourceDocument("Regional Culture Survey", "IndianRoadmap Editorial Team", "https://indianroadmap.local/sources/" + destination.slug(), SourceType.TRAVEL_PUBLICATION, true, now)
        ));
        story.setCreatedAt(now);
        story.setUpdatedAt(now);
        story.setPublishedAt(now);
        story.setEstimatedReadingTimeMinutes(readingTimeCalculator.calculate(story));
        return story;
    }

    private StoryType inferType(String destinationName) {
        String lowered = destinationName.toLowerCase();
        if (lowered.contains("temple") || lowered.contains("badrinath") || lowered.contains("kedarnath")
                || lowered.contains("amritsar") || lowered.contains("rameswaram")) {
            return StoryType.SPIRITUAL;
        }
        if (lowered.contains("fort") || lowered.contains("hampi") || lowered.contains("ajanta")
                || lowered.contains("ellora") || lowered.contains("khajuraho")) {
            return StoryType.HISTORY;
        }
        if (lowered.contains("lake") || lowered.contains("valley") || lowered.contains("falls")) {
            return StoryType.NATURE;
        }
        return StoryType.CULTURE;
    }

    private List<StoryChapterDocument> chaptersFor(String destinationName) {
        List<StoryChapterDocument> chapters = new ArrayList<>();
        chapters.add(chapter(destinationName, 1, "Origins of " + destinationName,
                "The earliest layers of " + destinationName + " emerge from geography, trade paths, and local memory. "
                        + "This chapter introduces the natural setting and early settlement patterns that shaped the destination.",
                "इस अध्याय में " + destinationName + " की प्रारंभिक ऐतिहासिक और भौगोलिक पृष्ठभूमि का परिचय दिया गया है।"));
        chapters.add(chapter(destinationName, 2, "Rise and Regional Influence",
                destinationName + " grew through regional patronage, craft traditions, and route connectivity. "
                        + "Communities, institutions, and rituals evolved to define a distinct local identity.",
                "यह अध्याय बताता है कि " + destinationName + " ने क्षेत्रीय प्रभाव, परंपरा और स्थानीय पहचान कैसे विकसित की।"));
        chapters.add(chapter(destinationName, 3, "Architecture and Living Culture",
                "Built spaces in " + destinationName + " reflect layered influences: local materials, climate-driven design, "
                        + "and artistic vocabulary carried across generations. Living culture continues through festivals and everyday practice.",
                "यह अध्याय " + destinationName + " की स्थापत्य शैली, लोक परंपराओं और सांस्कृतिक निरंतरता को समझाता है।"));
        chapters.add(chapter(destinationName, 4, "Transitions, Challenges, and Continuity",
                destinationName + " has seen transitions in governance, economy, and mobility. "
                        + "Despite change, continuity survives through language, food, and shared social memory.",
                "यह अध्याय परिवर्तन के दौर में " + destinationName + " की चुनौतियों और निरंतर सांस्कृतिक स्मृति पर केंद्रित है।"));
        chapters.add(chapter(destinationName, 5, destinationName + " Today",
                "Today, " + destinationName + " is both a travel destination and a lived cultural landscape. "
                        + "Responsible travel can strengthen local livelihoods while preserving heritage value for future generations.",
                "आज " + destinationName + " यात्रा और स्थानीय जीवन दोनों का केंद्र है; जिम्मेदार पर्यटन इसकी विरासत को सुरक्षित रख सकता है।"));
        return chapters;
    }

    private StoryChapterDocument chapter(String destinationName, int sequence, String title, String englishText, String hindiText) {
        String chapterId = destinationName.toLowerCase().replace(' ', '-') + "-chapter-" + sequence;
        List<StorySectionDocument> sections = List.of(
                new StorySectionDocument(chapterId + "-en", 1, title, englishText, StoryLanguage.ENGLISH),
                new StorySectionDocument(chapterId + "-hi", 2, title + " (Hindi)", hindiText, StoryLanguage.HINDI)
        );
        return new StoryChapterDocument(chapterId, sequence, title, sections);
    }

    private List<String> priorityStorySlugs() {
        return List.of(
                "hampi", "ajanta", "ellora", "mahabalipuram", "madurai", "thanjavur", "jaipur", "jodhpur", "jaisalmer", "udaipur",
                "khajuraho", "sanchi", "konark", "puri", "bodh-gaya", "amritsar", "kolkata", "hyderabad", "warangal", "ramappa",
                "mysuru", "kochi", "panaji", "srinagar", "leh", "dharamshala", "rishikesh", "varanasi", "delhi", "mumbai",
                "pune", "nashik", "gwalior", "orchha", "bhopal", "ahmedabad", "dwarka", "somnath", "chennai", "rameswaram",
                "tirupati", "visakhapatnam", "guwahati", "shillong", "darjeeling", "gangtok", "shimla", "manali", "kedarnath", "badrinath",
                "bhubaneswar", "cuttack", "patna", "rajgir", "nalanda", "chittorgarh", "pushkar", "ajmer", "coorg", "munnar"
        );
    }
}
