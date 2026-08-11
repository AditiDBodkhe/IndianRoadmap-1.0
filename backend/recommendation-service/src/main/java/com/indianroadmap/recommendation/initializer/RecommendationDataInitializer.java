package com.indianroadmap.recommendation.initializer;

import com.indianroadmap.recommendation.client.DestinationClient;
import com.indianroadmap.recommendation.document.*;
import com.indianroadmap.recommendation.repository.RecommendationProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Development-only data seeder.
 * Seeds recommendation profiles for known destinations.
 * Only runs when profiles are missing and destination-service is reachable.
 * Does NOT run in test or production profiles.
 */
@Component
@Profile("!test")
public class RecommendationDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RecommendationDataInitializer.class);

    private final RecommendationProfileRepository profileRepository;
    private final DestinationClient destinationClient;
    private final Clock clock;

    public RecommendationDataInitializer(
            RecommendationProfileRepository profileRepository,
            DestinationClient destinationClient,
            Clock clock) {
        this.profileRepository = profileRepository;
        this.destinationClient = destinationClient;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (profileRepository.count() > 0) {
            log.info("Recommendation profiles already exist. Skipping seed.");
            return;
        }

        log.info("Seeding recommendation profiles...");

        List<ProfileSeed> seeds = List.of(
                new ProfileSeed("chhitkul",
                        List.of(Mood.ADVENTUROUS, Mood.SOLITUDE, Mood.OFFBEAT),
                        List.of(Interest.VILLAGES, Interest.NATURE, Interest.PHOTOGRAPHY, Interest.HISTORY),
                        List.of(TravelStyle.BACKPACKER, TravelStyle.SLOW_TRAVEL, TravelStyle.OFFBEAT),
                        List.of("KINNAUR", "HIMACHAL PRADESH", "HIMALAYAS"),
                        3, 7, 8000, 20000,
                        List.of(Season.SUMMER, Season.AUTUMN),
                        "MODERATE"),

                new ProfileSeed("tabo",
                        List.of(Mood.SPIRITUAL, Mood.CURIOUS, Mood.CULTURAL),
                        List.of(Interest.SPIRITUALITY, Interest.CULTURE, Interest.HISTORY, Interest.ARCHITECTURE),
                        List.of(TravelStyle.SLOW_TRAVEL, TravelStyle.SOLO, TravelStyle.OFFBEAT),
                        List.of("SPITI", "HIMACHAL PRADESH", "HIMALAYAS"),
                        2, 5, 6000, 15000,
                        List.of(Season.SUMMER, Season.AUTUMN),
                        "MODERATE"),

                new ProfileSeed("kaza",
                        List.of(Mood.ADVENTUROUS, Mood.OFFBEAT, Mood.CURIOUS),
                        List.of(Interest.ADVENTURE, Interest.MOUNTAINS, Interest.LOCAL_LIFE, Interest.PHOTOGRAPHY),
                        List.of(TravelStyle.BACKPACKER, TravelStyle.ROAD_TRIP, TravelStyle.ADVENTURE),
                        List.of("SPITI", "HIMACHAL PRADESH", "HIMALAYAS"),
                        3, 6, 8000, 18000,
                        List.of(Season.SUMMER, Season.AUTUMN),
                        "MODERATE"),

                new ProfileSeed("hanle",
                        List.of(Mood.SOLITUDE, Mood.CURIOUS, Mood.ZEN),
                        List.of(Interest.ASTRONOMY, Interest.NATURE, Interest.PHOTOGRAPHY, Interest.LOCAL_LIFE),
                        List.of(TravelStyle.SLOW_TRAVEL, TravelStyle.OFFBEAT, TravelStyle.SOLO),
                        List.of("LADAKH", "LEH", "HIMALAYAS"),
                        2, 5, 10000, 25000,
                        List.of(Season.SUMMER, Season.AUTUMN),
                        "HARD"),

                new ProfileSeed("turtuk",
                        List.of(Mood.CURIOUS, Mood.OFFBEAT, Mood.CULTURAL),
                        List.of(Interest.LOCAL_LIFE, Interest.VILLAGES, Interest.CULTURE, Interest.HISTORY),
                        List.of(TravelStyle.BACKPACKER, TravelStyle.SLOW_TRAVEL, TravelStyle.SOLO),
                        List.of("NUBRA", "LADAKH", "HIMALAYAS"),
                        2, 4, 8000, 20000,
                        List.of(Season.SUMMER, Season.AUTUMN),
                        "MODERATE"),

                new ProfileSeed("mana",
                        List.of(Mood.SPIRITUAL, Mood.CULTURAL, Mood.CURIOUS),
                        List.of(Interest.SPIRITUALITY, Interest.HISTORY, Interest.CULTURE, Interest.NATURE),
                        List.of(TravelStyle.SLOW_TRAVEL, TravelStyle.SOLO, TravelStyle.COUPLE),
                        List.of("GARHWAL", "UTTARAKHAND", "HIMALAYAS"),
                        1, 3, 5000, 15000,
                        List.of(Season.SUMMER, Season.SPRING),
                        "EASY"),

                new ProfileSeed("munsiyari",
                        List.of(Mood.ADVENTUROUS, Mood.OFFBEAT, Mood.SOLITUDE),
                        List.of(Interest.MOUNTAINS, Interest.NATURE, Interest.ADVENTURE, Interest.PHOTOGRAPHY),
                        List.of(TravelStyle.BACKPACKER, TravelStyle.ADVENTURE, TravelStyle.ROAD_TRIP),
                        List.of("KUMAON", "UTTARAKHAND", "HIMALAYAS"),
                        3, 7, 7000, 18000,
                        List.of(Season.SUMMER, Season.SPRING, Season.AUTUMN),
                        "MODERATE"),

                new ProfileSeed("adi-kailash",
                        List.of(Mood.SPIRITUAL, Mood.ZEN, Mood.SOLITUDE),
                        List.of(Interest.SPIRITUALITY, Interest.MOUNTAINS, Interest.NATURE, Interest.CULTURE),
                        List.of(TravelStyle.SLOW_TRAVEL, TravelStyle.SOLO, TravelStyle.ADVENTURE),
                        List.of("KUMAON", "UTTARAKHAND", "HIMALAYAS"),
                        5, 10, 12000, 30000,
                        List.of(Season.SUMMER),
                        "HARD"),

                new ProfileSeed("amritsar",
                        List.of(Mood.SPIRITUAL, Mood.CULTURAL, Mood.SOCIAL),
                        List.of(Interest.SPIRITUALITY, Interest.HISTORY, Interest.FOOD, Interest.CULTURE, Interest.ARCHITECTURE),
                        List.of(TravelStyle.FAMILY, TravelStyle.COUPLE, TravelStyle.SOLO),
                        List.of("PUNJAB", "NORTH INDIA"),
                        2, 4, 5000, 15000,
                        List.of(Season.WINTER, Season.AUTUMN, Season.SPRING),
                        "EASY"),

                new ProfileSeed("hussainiwala",
                        List.of(Mood.CULTURAL, Mood.SOLITUDE, Mood.OFFBEAT),
                        List.of(Interest.HISTORY, Interest.LOCAL_LIFE, Interest.PHOTOGRAPHY, Interest.CULTURE),
                        List.of(TravelStyle.SOLO, TravelStyle.ROAD_TRIP, TravelStyle.SLOW_TRAVEL),
                        List.of("PUNJAB", "NORTH INDIA"),
                        1, 2, 3000, 8000,
                        List.of(Season.WINTER, Season.SPRING, Season.AUTUMN),
                        "EASY"),

                new ProfileSeed("pangong-tso",
                        List.of(Mood.ADVENTUROUS, Mood.ZEN, Mood.ROMANTIC),
                        List.of(Interest.NATURE, Interest.PHOTOGRAPHY, Interest.ADVENTURE, Interest.ASTRONOMY),
                        List.of(TravelStyle.ROAD_TRIP, TravelStyle.COUPLE, TravelStyle.ADVENTURE, TravelStyle.BACKPACKER),
                        List.of("LADAKH", "LEH", "HIMALAYAS"),
                        2, 5, 10000, 25000,
                        List.of(Season.SUMMER, Season.AUTUMN),
                        "MODERATE")
        );

        int seeded = 0;
        for (ProfileSeed seed : seeds) {
            try {
                var destOpt = destinationClient.getDestination(seed.destinationId());
                if (destOpt.isEmpty()) {
                    log.warn("Destination '{}' not found in destination-service. Skipping profile.", seed.destinationId());
                    continue;
                }
                if (profileRepository.existsByDestinationId(seed.destinationId())) {
                    log.debug("Profile already exists for '{}'. Skipping.", seed.destinationId());
                    continue;
                }

                var doc = new RecommendationProfileDocument();
                doc.setDestinationId(seed.destinationId());
                doc.setMoods(seed.moods());
                doc.setInterests(seed.interests());
                doc.setTravelStyles(seed.travelStyles());
                doc.setRegions(seed.regions());
                doc.setIdealDurationMin(seed.durationMin());
                doc.setIdealDurationMax(seed.durationMax());
                doc.setBudgetMin(seed.budgetMin());
                doc.setBudgetMax(seed.budgetMax());
                doc.setSeasonTags(seed.seasons());
                doc.setDifficulty(seed.difficulty());
                doc.setWeight(1.0);
                Instant now = Instant.now(clock);
                doc.setCreatedAt(now);
                doc.setUpdatedAt(now);
                profileRepository.save(doc);
                seeded++;
                log.info("Seeded recommendation profile for '{}'", seed.destinationId());
            } catch (Exception ex) {
                log.warn("Could not seed profile for '{}': {}", seed.destinationId(), ex.getMessage());
            }
        }

        log.info("Recommendation seed complete. {} profiles created.", seeded);
    }

    private record ProfileSeed(
            String destinationId,
            List<Mood> moods,
            List<Interest> interests,
            List<TravelStyle> travelStyles,
            List<String> regions,
            int durationMin,
            int durationMax,
            int budgetMin,
            int budgetMax,
            List<Season> seasons,
            String difficulty
    ) {}
}
