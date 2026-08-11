package com.indianroadmap.roadmap.initializer;

import com.indianroadmap.roadmap.client.DestinationClient;
import com.indianroadmap.roadmap.client.DestinationSummary;
import com.indianroadmap.roadmap.document.RoadType;
import com.indianroadmap.roadmap.document.RoadmapDocument;
import com.indianroadmap.roadmap.document.RoadmapEdgeDocument;
import com.indianroadmap.roadmap.document.RoadmapNodeDocument;
import com.indianroadmap.roadmap.document.RoadmapNodeRole;
import com.indianroadmap.roadmap.document.RoadmapStatus;
import com.indianroadmap.roadmap.document.RouteDifficulty;
import com.indianroadmap.roadmap.document.RouteSummaryDocument;
import com.indianroadmap.roadmap.repository.RoadmapRepository;
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
import java.util.UUID;

@Component
@Profile({"local", "dev"})
public class RoadmapDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(RoadmapDataInitializer.class);

    private final RoadmapRepository roadmapRepository;
    private final DestinationClient destinationClient;
    private final Clock clock;

    public RoadmapDataInitializer(RoadmapRepository roadmapRepository, DestinationClient destinationClient, Clock clock) {
        this.roadmapRepository = roadmapRepository;
        this.destinationClient = destinationClient;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (!waitForDestinationService()) {
            log.warn("Skipping roadmap seeding because destination-service is not reachable during startup");
            return;
        }
        for (RoadmapSeed seed : roadmapSeeds()) {
            if (roadmapRepository.existsBySlug(seed.slug())) {
                continue;
            }
            try {
                RoadmapDocument doc = buildRoadmap(seed);
                if (doc.getNodes().size() >= 3) {
                    roadmapRepository.save(doc);
                    log.info("Seeded roadmap: {} ({} nodes)", seed.slug(), doc.getNodes().size());
                } else {
                    log.warn("Skipped roadmap {} due to insufficient resolved destinations", seed.slug());
                }
            } catch (Exception ex) {
                log.warn("Skipping roadmap {}: {}", seed.slug(), ex.getMessage());
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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private RoadmapDocument buildRoadmap(RoadmapSeed seed) {
        Instant now = Instant.now(clock);
        RoadmapDocument document = new RoadmapDocument();
        document.setSlug(seed.slug());
        document.setName(seed.name());
        document.setDescription(seed.description());
        document.setStatus(RoadmapStatus.PUBLISHED);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        List<RoadmapNodeDocument> nodes = new ArrayList<>();
        for (int i = 0; i < seed.destinationSlugs().size(); i++) {
            String slug = seed.destinationSlugs().get(i);
            DestinationSummary destination = destinationClient.getDestinationBySlug(slug);
            RoadmapNodeRole role = i == 0
                    ? RoadmapNodeRole.START
                    : (i == seed.destinationSlugs().size() - 1 ? RoadmapNodeRole.END : RoadmapNodeRole.WAYPOINT);
            nodes.add(new RoadmapNodeDocument(
                    UUID.randomUUID().toString(),
                    destination.id(),
                    i + 1,
                    destination.name(),
                    role,
                    destination.elevationMeters()
            ));
        }
        document.setNodes(nodes);

        List<RoadmapEdgeDocument> edges = new ArrayList<>();
        for (int i = 0; i < nodes.size() - 1; i++) {
            DestinationSummary from = destinationClient.getDestinationBySlug(seed.destinationSlugs().get(i));
            DestinationSummary to = destinationClient.getDestinationBySlug(seed.destinationSlugs().get(i + 1));
            double distance = haversineKm(from.latitude(), from.longitude(), to.latitude(), to.longitude());
            int travelMinutes = (int) Math.max(45, Math.round((distance / 42.0) * 60.0));
            edges.add(new RoadmapEdgeDocument(
                    UUID.randomUUID().toString(),
                    nodes.get(i).getNodeId(),
                    nodes.get(i + 1).getNodeId(),
                    round(distance),
                    travelMinutes,
                    inferRoadType(from.elevationMeters(), to.elevationMeters()),
                    inferDifficulty(from.elevationMeters(), to.elevationMeters(), distance)
            ));
        }
        document.setEdges(edges);
        document.setRouteSummary(summary(nodes, edges));
        return document;
    }

    private RouteSummaryDocument summary(List<RoadmapNodeDocument> nodes, List<RoadmapEdgeDocument> edges) {
        RouteSummaryDocument result = new RouteSummaryDocument();
        result.setNodeCount(nodes.size());
        result.setEdgeCount(edges.size());
        result.setTotalDistanceKm(round(edges.stream().mapToDouble(RoadmapEdgeDocument::getDistanceKm).sum()));
        result.setTotalTravelTimeMinutes(edges.stream().mapToInt(RoadmapEdgeDocument::getEstimatedTravelTimeMinutes).sum());
        int highest = nodes.stream().mapToInt(RoadmapNodeDocument::getElevationMeters).max().orElse(0);
        int lowest = nodes.stream().mapToInt(RoadmapNodeDocument::getElevationMeters).min().orElse(0);
        int gain = 0;
        for (int i = 1; i < nodes.size(); i++) {
            int diff = nodes.get(i).getElevationMeters() - nodes.get(i - 1).getElevationMeters();
            if (diff > 0) gain += diff;
        }
        result.setHighestElevationMeters(highest);
        result.setLowestElevationMeters(lowest);
        result.setElevationGainMeters(gain);
        return result;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private RoadType inferRoadType(int fromElevation, int toElevation) {
        int max = Math.max(fromElevation, toElevation);
        if (max >= 2500) return RoadType.MOUNTAIN_ROAD;
        if (max >= 1200) return RoadType.STATE_HIGHWAY;
        return RoadType.HIGHWAY;
    }

    private RouteDifficulty inferDifficulty(int fromElevation, int toElevation, double distanceKm) {
        int elevationDelta = Math.abs(toElevation - fromElevation);
        if (Math.max(fromElevation, toElevation) >= 3200 || elevationDelta >= 1200) return RouteDifficulty.DIFFICULT;
        if (distanceKm >= 260 || elevationDelta >= 500) return RouteDifficulty.MODERATE;
        return RouteDifficulty.EASY;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    private List<RoadmapSeed> roadmapSeeds() {
        return List.of(
                new RoadmapSeed("rajasthan-heritage-road-trip", "Rajasthan Heritage Trail",
                        "A journey through forts, stepwells, desert culture, and palace cities in Rajasthan.",
                        List.of("jaipur", "pushkar", "jodhpur", "jaisalmer", "udaipur")),
                new RoadmapSeed("kerala-backwaters-culture", "Kerala Backwaters & Culture",
                        "Lagoons, spice hills, heritage ports, and slow-travel waterside stays.",
                        List.of("kochi", "kumarakom", "alleppey", "thekkady", "munnar", "varkala")),
                new RoadmapSeed("karnataka-heritage-trail", "Karnataka Heritage Trail",
                        "From imperial ruins and Hoysala temples to coast and coffee highlands.",
                        List.of("bengaluru", "mysuru", "hampi", "pattadakal", "badami", "aihole")),
                new RoadmapSeed("maharashtra-fort-trail", "Maharashtra Fort Trail",
                        "A western India route connecting maritime forts, ghats, and Deccan heritage.",
                        List.of("mumbai", "lonavala", "raigad", "pune", "nashik", "aurangabad")),
                new RoadmapSeed("tamil-nadu-temple-trail", "Tamil Nadu Temple Trail",
                        "Sacred architecture from Dravidian temple towns to ocean-edge pilgrimage.",
                        List.of("chennai", "mahabalipuram", "kanchipuram", "thanjavur", "madurai", "rameswaram")),
                new RoadmapSeed("himachal-mountain-journey", "Himachal Mountain Journey",
                        "Valley towns, monastic landscapes, and high mountain roads.",
                        List.of("shimla", "manali", "kasol", "dharamshala", "spiti", "kinnaur")),
                new RoadmapSeed("uttarakhand-spiritual-trail", "Uttarakhand Spiritual Trail",
                        "River pilgrimages, mountain shrines, and quiet hill traditions.",
                        List.of("haridwar", "rishikesh", "kedarnath", "badrinath", "auli", "nainital")),
                new RoadmapSeed("northeast-discovery-roadmap", "Northeast Discovery",
                        "River islands, cloud valleys, and biodiversity-rich landscapes.",
                        List.of("guwahati", "kaziranga", "majuli", "shillong", "cherrapunji", "dawki")),
                new RoadmapSeed("goa-konkan-coastal-road", "Goa & Konkan Coastal Road",
                        "A coastal arc of beach towns, ports, and seafood heritage.",
                        List.of("mumbai", "alibaug", "ratnagiri", "ganpatipule", "panaji", "palolem")),
                new RoadmapSeed("gujarat-heritage-circuit", "Gujarat Heritage Circuit",
                        "Sun temples, stepwells, coast shrines, and craft regions.",
                        List.of("ahmedabad", "patan", "modhera", "dwarka", "somnath", "bhuj")),
                new RoadmapSeed("madhya-pradesh-heritage-trail", "Madhya Pradesh Heritage Trail",
                        "Rock-cut temples, Buddhist memory sites, and central-India forts.",
                        List.of("bhopal", "sanchi", "khajuraho", "orchha", "gwalior", "ujjain")),
                new RoadmapSeed("odisha-temple-trail", "Odisha Temple Trail",
                        "A ritual landscape of sacred cities, coastal shrines, and classical arts.",
                        List.of("bhubaneswar", "konark", "puri", "cuttack", "chilika")),
                new RoadmapSeed("andhra-heritage-trail", "Andhra Pradesh Heritage Trail",
                        "Buddhist traces, coastal hills, and pilgrimage centres.",
                        List.of("visakhapatnam", "araku-valley", "amaravati", "vijayawada", "tirupati")),
                new RoadmapSeed("telangana-heritage-trail", "Telangana Heritage Trail",
                        "Kakatiya heritage, granite plateaus, and Deccan cultural memory.",
                        List.of("hyderabad", "warangal", "ramappa", "nagarjuna-sagar", "bhadrachalam")),
                new RoadmapSeed("punjab-heritage-journey", "Punjab Heritage Journey",
                        "Faith, memory, and living culinary traditions across Punjab.",
                        List.of("amritsar", "anandpur-sahib", "patiala", "ludhiana")),
                new RoadmapSeed("west-bengal-culture-trail", "West Bengal Culture Trail",
                        "Colonial-era urbanism, literary centres, and mountain tea routes.",
                        List.of("kolkata", "shantiniketan", "darjeeling", "kalimpong", "digha")),
                new RoadmapSeed("bihar-buddhist-trail", "Bihar Buddhist Trail",
                        "Pilgrimage route through the heartland of Buddhist history.",
                        List.of("patna", "nalanda", "rajgir", "bodh-gaya", "vaishali")),
                new RoadmapSeed("kashmir-valley-journey", "Kashmir Valley Journey",
                        "Lakes, meadows, and valley heritage shaped by mountain culture.",
                        List.of("srinagar", "gulmarg", "pahalgam", "sonamarg", "jammu")),
                new RoadmapSeed("ladakh-high-altitude-journey", "Ladakh High-Altitude Journey",
                        "A high mountain route of passes, lakes, and monastic settlements.",
                        List.of("leh", "khardung-la", "nubra-valley", "pangong-lake", "tso-moriri")),
                new RoadmapSeed("sikkim-mountain-journey", "Sikkim Mountain Journey",
                        "Cloud forests, monastery routes, and alpine valleys.",
                        List.of("gangtok", "pelling", "namchi", "lachung", "yumthang"))
        );
    }

    private record RoadmapSeed(String slug, String name, String description, List<String> destinationSlugs) {}
}
