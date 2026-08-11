package com.indianroadmap.destination.config;

import com.indianroadmap.destination.document.ArchitectureInformation;
import com.indianroadmap.destination.document.Attraction;
import com.indianroadmap.destination.document.CulturalInformation;
import com.indianroadmap.destination.document.DestinationCategory;
import com.indianroadmap.destination.document.DestinationDocument;
import com.indianroadmap.destination.document.DestinationName;
import com.indianroadmap.destination.document.Elevation;
import com.indianroadmap.destination.document.HistoricalHighlight;
import com.indianroadmap.destination.document.ImageReference;
import com.indianroadmap.destination.document.Language;
import com.indianroadmap.destination.document.Mood;
import com.indianroadmap.destination.document.SourceReference;
import com.indianroadmap.destination.repository.DestinationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Profile({"local", "dev"})
public class DestinationDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DestinationDataInitializer.class);

    private final DestinationRepository repository;
    private final Clock clock;

    public DestinationDataInitializer(DestinationRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        Instant now = Instant.now(clock);
        List<SeedDestination> all = seedDestinations();
        int inserted = 0;
        for (SeedDestination seed : all) {
            if (repository.existsBySlug(seed.slug())) {
                continue;
            }
            repository.save(toDocument(seed, now));
            inserted++;
        }
        log.info("Destination seeding complete. inserted={}, totalSeeds={}, totalInDb={}", inserted, all.size(), repository.count());
    }

    private DestinationDocument toDocument(SeedDestination seed, Instant now) {
        var doc = new DestinationDocument();
        doc.setSlug(seed.slug());
        doc.setName(new DestinationName(seed.name(), seed.name()));
        doc.setState(seed.state());
        doc.setDistrict(seed.district());
        doc.setRegion(seed.region());
        doc.setShortDescription("A culturally rich destination in " + seed.state() + " known for its living traditions and regional identity.");
        doc.setDescription(longDescription(seed));

        double[] centroid = stateCentroids().getOrDefault(seed.state(), new double[]{22.0, 79.0});
        double latitude = centroid[0] + normalizedJitter(seed.slug(), 0.8);
        double longitude = centroid[1] + normalizedJitter(seed.name(), 1.1);
        doc.setCoordinates(new GeoJsonPoint(longitude, latitude));

        int elevationMeters = estimateElevation(seed);
        doc.setElevation(new Elevation(elevationMeters, (int) Math.round(elevationMeters * 3.28084)));

        List<DestinationCategory> categories = categoriesFor(seed);
        doc.setCategories(categories);
        doc.setMoods(moodsFor(categories, seed));
        doc.setLanguages(languagesFor(seed.state()));

        doc.setHistoricalHighlights(List.of(
                new HistoricalHighlight("Early Settlements", "Landscape and early habitation", "The region around " + seed.name() + " has hosted settlement patterns shaped by trade routes, water access, and agrarian rhythms."),
                new HistoricalHighlight("Regional Influence", "Cultural consolidation", "Over centuries, " + seed.name() + " absorbed courtly, sacred, and vernacular traditions visible in festivals, foodways, and architecture."),
                new HistoricalHighlight("Contemporary Era", "Living heritage", "Today, " + seed.name() + " balances tourism, local livelihood, and heritage stewardship through evolving community participation.")
        ));

        doc.setCulturalInformation(new CulturalInformation(
                "Local cultural life blends seasonal rituals, market traditions, and community gatherings across neighborhoods and surrounding villages.",
                cuisineFor(seed.state()),
                festivalsFor(seed.state()),
                "Attire reflects climate, local weaving practices, and ceremonial dress traditions.",
                "Travel respectfully, engage local guides where possible, and support community-led cultural enterprises."
        ));

        doc.setArchitecture(new ArchitectureInformation(
                architectureStyle(categories),
                "Stone, lime plaster, timber, and regionally available materials",
                "Multiple historical phases shaped by regional dynasties and modern adaptive reuse",
                "Layered streetscapes, sacred structures, civic landmarks, and vernacular neighborhoods"
        ));

        doc.setAttractions(attractionsFor(seed));
        doc.setImages(imagesFor(seed));
        doc.setSources(List.of(
                new SourceReference("Incredible India destination brief", "India Tourism", "https://www.incredibleindia.gov.in", now),
                new SourceReference("State tourism handbook", seed.state() + " Tourism", "https://tourism.gov.in", now),
                new SourceReference("Regional heritage survey notes", "IndianRoadmap Editorial", "https://indianroadmap.local/sources/" + seed.slug(), now)
        ));

        doc.setVerified(true);
        doc.setLastVerifiedAt(now);
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);
        return doc;
    }

    private String longDescription(SeedDestination seed) {
        return seed.name() + " in " + seed.state() + " is presented as a discovery-led destination combining place memory, local livelihoods, "
                + "and layered cultural landscapes. Travelers can explore heritage precincts, local food circuits, and nearby natural zones while "
                + "engaging responsibly with resident communities and regional ecology.";
    }

    private int estimateElevation(SeedDestination seed) {
        int base = switch (seed.state()) {
            case "Ladakh" -> 3400;
            case "Himachal Pradesh", "Uttarakhand", "Sikkim" -> 1700;
            case "Jammu & Kashmir" -> 1600;
            case "Meghalaya", "Nagaland", "Mizoram", "Arunachal Pradesh" -> 1200;
            case "Kerala", "Karnataka", "Tamil Nadu" -> 450;
            case "Rajasthan", "Madhya Pradesh", "Telangana" -> 380;
            default -> 220;
        };
        return Math.max(40, base + Math.abs(seed.slug().hashCode() % 700));
    }

    private List<DestinationCategory> categoriesFor(SeedDestination seed) {
        String name = seed.name().toLowerCase(Locale.ROOT);
        List<DestinationCategory> result = new ArrayList<>();
        if (name.contains("fort") || name.contains("garh") || name.contains("heritage")) result.add(DestinationCategory.HERITAGE);
        if (name.contains("temple") || name.contains("kedarnath") || name.contains("badrinath") || name.contains("rameswaram")
                || name.contains("amritsar") || name.contains("dwarka") || name.contains("somnath")) result.add(DestinationCategory.SPIRITUAL);
        if (name.contains("lake") || name.contains("valley") || name.contains("falls") || name.contains("hill") || name.contains("mount")
                || name.contains("kasol") || name.contains("munnar")) result.add(DestinationCategory.MOUNTAIN);
        if (name.contains("beach") || name.contains("coastal") || name.contains("goa") || name.contains("alibaug")
                || name.contains("varkala") || name.contains("kovalam") || name.contains("konkan")) result.add(DestinationCategory.ADVENTURE);
        if (name.contains("cave") || name.contains("ajanta") || name.contains("ellora") || name.contains("hampi")
                || name.contains("khajuraho") || name.contains("mahabalipuram") || name.contains("konark")) result.add(DestinationCategory.HISTORICAL);
        if (result.isEmpty()) result.add(DestinationCategory.CITY);
        if (!result.contains(DestinationCategory.HERITAGE) && (seed.state().equals("Rajasthan") || seed.state().equals("Gujarat"))) {
            result.add(DestinationCategory.HERITAGE);
        }
        return List.copyOf(result.stream().distinct().toList());
    }

    private List<Mood> moodsFor(List<DestinationCategory> categories, SeedDestination seed) {
        List<Mood> moods = new ArrayList<>();
        if (categories.contains(DestinationCategory.SPIRITUAL)) moods.add(Mood.SPIRITUAL);
        if (categories.contains(DestinationCategory.ADVENTURE) || categories.contains(DestinationCategory.MOUNTAIN)) moods.add(Mood.ADVENTURE);
        if (categories.contains(DestinationCategory.HERITAGE) || categories.contains(DestinationCategory.HISTORICAL)) moods.add(Mood.HERITAGE);
        if (seed.state().equals("Ladakh") || seed.state().equals("Sikkim") || seed.state().equals("Himachal Pradesh")) moods.add(Mood.SOLITUDE);
        if (moods.isEmpty()) moods.add(Mood.CURIOUS);
        if (!moods.contains(Mood.CURIOUS)) moods.add(Mood.CURIOUS);
        return List.copyOf(moods.stream().distinct().limit(3).toList());
    }

    private List<Language> languagesFor(String state) {
        return switch (state) {
            case "Ladakh" -> List.of(Language.ENGLISH, Language.HINDI, Language.LADAKHI);
            case "Himachal Pradesh" -> List.of(Language.ENGLISH, Language.HINDI, Language.KINNAURI);
            case "Uttarakhand" -> List.of(Language.ENGLISH, Language.HINDI, Language.GARHWALI, Language.KUMAONI);
            case "Punjab" -> List.of(Language.ENGLISH, Language.HINDI, Language.PUNJABI);
            default -> List.of(Language.ENGLISH, Language.HINDI);
        };
    }

    private List<Attraction> attractionsFor(SeedDestination seed) {
        return List.of(
                new Attraction(seed.name() + " Heritage Quarter", "HERITAGE", "Walkable precinct with layered architecture and local narratives."),
                new Attraction(seed.name() + " Cultural Core", "CULTURE", "Public spaces where local crafts, performance, and festivals converge."),
                new Attraction(seed.name() + " Nature Belt", "NATURE", "Accessible natural zone ideal for sunrise, walks, and seasonal views."),
                new Attraction(seed.name() + " Local Market Trail", "MARKET", "Food, textiles, and artisanal goods with region-specific character."),
                new Attraction(seed.name() + " Story Landmark", "LANDMARK", "A site frequently referenced in local memory and visitor storytelling.")
        );
    }

    private List<ImageReference> imagesFor(SeedDestination seed) {
        return List.of(
                new ImageReference("https://images.indianroadmap.local/" + seed.slug() + "/hero.jpg", seed.name() + " skyline", "IndianRoadmap", "IndianRoadmap Archive"),
                new ImageReference("https://images.indianroadmap.local/" + seed.slug() + "/culture.jpg", "Cultural streetscape in " + seed.name(), "IndianRoadmap", "IndianRoadmap Archive"),
                new ImageReference("https://images.indianroadmap.local/" + seed.slug() + "/landscape.jpg", "Regional landscape around " + seed.name(), "IndianRoadmap", "IndianRoadmap Archive")
        );
    }

    private List<String> festivalsFor(String state) {
        return switch (state) {
            case "Rajasthan" -> List.of("Gangaur", "Desert Festival", "Teej");
            case "Tamil Nadu" -> List.of("Pongal", "Chithirai Festival", "Navaratri");
            case "Kerala" -> List.of("Onam", "Thrissur Pooram", "Vishu");
            case "Punjab" -> List.of("Baisakhi", "Lohri", "Gurpurab");
            case "West Bengal" -> List.of("Durga Puja", "Poila Boishakh", "Kali Puja");
            default -> List.of("Navratri", "Diwali", "Regional Harvest Festival");
        };
    }

    private String cuisineFor(String state) {
        return switch (state) {
            case "Maharashtra" -> "Maharashtrian thalis, coastal seafood, millet flatbreads, and festive sweets.";
            case "Karnataka" -> "Udupi classics, millet dishes, temple prasada traditions, and coffee culture.";
            case "Kerala" -> "Backwater seafood, coconut-forward curries, appam traditions, and spice-rich meals.";
            case "Tamil Nadu" -> "Temple cuisine, rice-based meals, Chettinad influences, and tiffin culture.";
            case "Rajasthan" -> "Desert-adapted cuisine with lentil-rich dishes, breads, and festive sweets.";
            default -> "Regional cuisine shaped by climate, agrarian cycles, and local spice traditions.";
        };
    }

    private String architectureStyle(List<DestinationCategory> categories) {
        if (categories.contains(DestinationCategory.SPIRITUAL)) return "Sacred architecture with layered ritual spaces";
        if (categories.contains(DestinationCategory.HERITAGE) || categories.contains(DestinationCategory.HISTORICAL)) {
            return "Heritage urbanism with palace/fort-temple influences";
        }
        if (categories.contains(DestinationCategory.MOUNTAIN)) return "Mountain vernacular adapted to slope and climate";
        return "Regional civic and vernacular architecture";
    }

    private double normalizedJitter(String key, double scale) {
        int hash = Math.abs(key.hashCode() % 1000);
        return ((hash / 1000.0) - 0.5) * scale;
    }

    private Map<String, double[]> stateCentroids() {
        Map<String, double[]> map = new HashMap<>();
        map.put("Maharashtra", new double[]{19.6, 75.3});
        map.put("Karnataka", new double[]{14.5, 75.7});
        map.put("Kerala", new double[]{10.5, 76.3});
        map.put("Tamil Nadu", new double[]{11.2, 78.6});
        map.put("Rajasthan", new double[]{26.9, 73.8});
        map.put("Himachal Pradesh", new double[]{31.9, 77.2});
        map.put("Uttarakhand", new double[]{30.2, 79.3});
        map.put("Gujarat", new double[]{22.3, 71.7});
        map.put("Madhya Pradesh", new double[]{23.5, 78.4});
        map.put("Andhra Pradesh", new double[]{15.9, 79.7});
        map.put("Telangana", new double[]{17.9, 79.3});
        map.put("Odisha", new double[]{20.3, 85.8});
        map.put("West Bengal", new double[]{23.0, 87.9});
        map.put("Bihar", new double[]{25.8, 85.8});
        map.put("Punjab", new double[]{31.0, 75.4});
        map.put("Jammu & Kashmir", new double[]{33.8, 75.2});
        map.put("Ladakh", new double[]{34.4, 77.6});
        map.put("Sikkim", new double[]{27.5, 88.5});
        map.put("Assam", new double[]{26.2, 92.9});
        map.put("Meghalaya", new double[]{25.5, 91.3});
        map.put("Goa", new double[]{15.4, 74.0});
        map.put("Delhi", new double[]{28.6, 77.2});
        map.put("Uttar Pradesh", new double[]{27.2, 80.9});
        map.put("Chhattisgarh", new double[]{21.3, 82.0});
        map.put("Jharkhand", new double[]{23.6, 85.3});
        map.put("Arunachal Pradesh", new double[]{28.1, 94.7});
        map.put("Nagaland", new double[]{26.1, 94.3});
        map.put("Mizoram", new double[]{23.2, 92.8});
        map.put("Manipur", new double[]{24.6, 93.9});
        map.put("Tripura", new double[]{23.9, 91.9});
        return map;
    }

    private List<SeedDestination> seedDestinations() {
        String data = """
                mumbai|Mumbai|Maharashtra|Mumbai|Konkan
                pune|Pune|Maharashtra|Pune|Deccan
                nashik|Nashik|Maharashtra|Nashik|North Maharashtra
                lonavala|Lonavala|Maharashtra|Pune|Western Ghats
                mahabaleshwar|Mahabaleshwar|Maharashtra|Satara|Western Ghats
                alibaug|Alibaug|Maharashtra|Raigad|Konkan
                kolhapur|Kolhapur|Maharashtra|Kolhapur|South Maharashtra
                aurangabad|Aurangabad|Maharashtra|Chhatrapati Sambhajinagar|Marathwada
                ajanta|Ajanta|Maharashtra|Jalgaon|North Maharashtra
                ellora|Ellora|Maharashtra|Chhatrapati Sambhajinagar|Marathwada
                tadoba|Tadoba|Maharashtra|Chandrapur|Vidarbha
                ratnagiri|Ratnagiri|Maharashtra|Ratnagiri|Konkan
                ganpatipule|Ganpatipule|Maharashtra|Ratnagiri|Konkan
                matheran|Matheran|Maharashtra|Raigad|Konkan
                shirdi|Shirdi|Maharashtra|Ahmednagar|North Maharashtra
                raigad|Raigad|Maharashtra|Raigad|Konkan
                sindhudurg|Sindhudurg|Maharashtra|Sindhudurg|Konkan
                harihareshwar|Harihareshwar|Maharashtra|Raigad|Konkan
                bengaluru|Bengaluru|Karnataka|Bengaluru Urban|South Karnataka
                mysuru|Mysuru|Karnataka|Mysuru|South Karnataka
                hampi|Hampi|Karnataka|Vijayanagara|North Karnataka
                badami|Badami|Karnataka|Bagalkot|North Karnataka
                pattadakal|Pattadakal|Karnataka|Bagalkot|North Karnataka
                aihole|Aihole|Karnataka|Bagalkot|North Karnataka
                coorg|Coorg|Karnataka|Kodagu|Malnad
                chikmagalur|Chikmagalur|Karnataka|Chikkamagaluru|Malnad
                gokarna|Gokarna|Karnataka|Uttara Kannada|Karavali
                udupi|Udupi|Karnataka|Udupi|Karavali
                jog-falls|Jog Falls|Karnataka|Shivamogga|Malnad
                belur|Belur|Karnataka|Hassan|South Karnataka
                halebidu|Halebidu|Karnataka|Hassan|South Karnataka
                kabini|Kabini|Karnataka|Mysuru|South Karnataka
                bandipur|Bandipur|Karnataka|Chamarajanagar|South Karnataka
                kochi|Kochi|Kerala|Ernakulam|Central Kerala
                munnar|Munnar|Kerala|Idukki|High Ranges
                alleppey|Alleppey|Kerala|Alappuzha|Backwaters
                varkala|Varkala|Kerala|Thiruvananthapuram|South Kerala
                wayanad|Wayanad|Kerala|Wayanad|North Kerala
                thekkady|Thekkady|Kerala|Idukki|High Ranges
                kumarakom|Kumarakom|Kerala|Kottayam|Backwaters
                kovalam|Kovalam|Kerala|Thiruvananthapuram|South Kerala
                thrissur|Thrissur|Kerala|Thrissur|Central Kerala
                bekal|Bekal|Kerala|Kasaragod|North Kerala
                kannur|Kannur|Kerala|Kannur|North Kerala
                chennai|Chennai|Tamil Nadu|Chennai|North Tamil Nadu
                mahabalipuram|Mahabalipuram|Tamil Nadu|Chengalpattu|North Tamil Nadu
                madurai|Madurai|Tamil Nadu|Madurai|South Tamil Nadu
                thanjavur|Thanjavur|Tamil Nadu|Thanjavur|Cauvery Delta
                rameswaram|Rameswaram|Tamil Nadu|Ramanathapuram|South Tamil Nadu
                kanyakumari|Kanyakumari|Tamil Nadu|Kanyakumari|South Tamil Nadu
                ooty|Ooty|Tamil Nadu|Nilgiris|Western Ghats
                kodaikanal|Kodaikanal|Tamil Nadu|Dindigul|Western Ghats
                coimbatore|Coimbatore|Tamil Nadu|Coimbatore|West Tamil Nadu
                chidambaram|Chidambaram|Tamil Nadu|Cuddalore|Cauvery Delta
                kanchipuram|Kanchipuram|Tamil Nadu|Kanchipuram|North Tamil Nadu
                tiruchirappalli|Tiruchirappalli|Tamil Nadu|Tiruchirappalli|Cauvery Delta
                jaipur|Jaipur|Rajasthan|Jaipur|East Rajasthan
                jodhpur|Jodhpur|Rajasthan|Jodhpur|Marwar
                udaipur|Udaipur|Rajasthan|Udaipur|Mewar
                jaisalmer|Jaisalmer|Rajasthan|Jaisalmer|Thar
                pushkar|Pushkar|Rajasthan|Ajmer|Ajmer Region
                ajmer|Ajmer|Rajasthan|Ajmer|Ajmer Region
                bikaner|Bikaner|Rajasthan|Bikaner|North Rajasthan
                mount-abu|Mount Abu|Rajasthan|Sirohi|Aravalli
                chittorgarh|Chittorgarh|Rajasthan|Chittorgarh|Mewar
                ranthambore|Ranthambore|Rajasthan|Sawai Madhopur|East Rajasthan
                bundi|Bundi|Rajasthan|Bundi|Hadoti
                alwar|Alwar|Rajasthan|Alwar|East Rajasthan
                shimla|Shimla|Himachal Pradesh|Shimla|Shimla Hills
                manali|Manali|Himachal Pradesh|Kullu|Kullu Valley
                kasol|Kasol|Himachal Pradesh|Kullu|Parvati Valley
                dharamshala|Dharamshala|Himachal Pradesh|Kangra|Kangra
                mcleod-ganj|McLeod Ganj|Himachal Pradesh|Kangra|Kangra
                spiti|Spiti|Himachal Pradesh|Lahaul and Spiti|Spiti
                kinnaur|Kinnaur|Himachal Pradesh|Kinnaur|Kinnaur
                dalhousie|Dalhousie|Himachal Pradesh|Chamba|Chamba
                chamba|Chamba|Himachal Pradesh|Chamba|Chamba
                kullu|Kullu|Himachal Pradesh|Kullu|Kullu Valley
                rishikesh|Rishikesh|Uttarakhand|Dehradun|Garhwal
                haridwar|Haridwar|Uttarakhand|Haridwar|Garhwal
                mussoorie|Mussoorie|Uttarakhand|Dehradun|Garhwal
                nainital|Nainital|Uttarakhand|Nainital|Kumaon
                auli|Auli|Uttarakhand|Chamoli|Garhwal
                kedarnath|Kedarnath|Uttarakhand|Rudraprayag|Garhwal
                badrinath|Badrinath|Uttarakhand|Chamoli|Garhwal
                valley-of-flowers|Valley of Flowers|Uttarakhand|Chamoli|Garhwal
                almora|Almora|Uttarakhand|Almora|Kumaon
                ranikhet|Ranikhet|Uttarakhand|Almora|Kumaon
                ahmedabad|Ahmedabad|Gujarat|Ahmedabad|Central Gujarat
                vadodara|Vadodara|Gujarat|Vadodara|Central Gujarat
                surat|Surat|Gujarat|Surat|South Gujarat
                kutch|Kutch|Gujarat|Kutch|Kutch
                bhuj|Bhuj|Gujarat|Kutch|Kutch
                dwarka|Dwarka|Gujarat|Devbhumi Dwarka|Saurashtra
                somnath|Somnath|Gujarat|Gir Somnath|Saurashtra
                gir|Gir|Gujarat|Gir Somnath|Saurashtra
                modhera|Modhera|Gujarat|Mehsana|North Gujarat
                patan|Patan|Gujarat|Patan|North Gujarat
                champaner|Champaner|Gujarat|Panchmahal|Central Gujarat
                bhopal|Bhopal|Madhya Pradesh|Bhopal|Central India
                indore|Indore|Madhya Pradesh|Indore|Malwa
                ujjain|Ujjain|Madhya Pradesh|Ujjain|Malwa
                khajuraho|Khajuraho|Madhya Pradesh|Chhatarpur|Bundelkhand
                orchha|Orchha|Madhya Pradesh|Niwari|Bundelkhand
                gwalior|Gwalior|Madhya Pradesh|Gwalior|Gird
                sanchi|Sanchi|Madhya Pradesh|Raisen|Central India
                pachmarhi|Pachmarhi|Madhya Pradesh|Narmadapuram|Satpura
                kanha|Kanha|Madhya Pradesh|Mandla|Mahakoshal
                bandhavgarh|Bandhavgarh|Madhya Pradesh|Umaria|Baghelkhand
                mandu|Mandu|Madhya Pradesh|Dhar|Malwa
                visakhapatnam|Visakhapatnam|Andhra Pradesh|Visakhapatnam|Coastal Andhra
                araku-valley|Araku Valley|Andhra Pradesh|Alluri Sitharama Raju|Eastern Ghats
                vijayawada|Vijayawada|Andhra Pradesh|NTR|Coastal Andhra
                amaravati|Amaravati|Andhra Pradesh|Guntur|Coastal Andhra
                tirupati|Tirupati|Andhra Pradesh|Tirupati|Rayalaseema
                gandikota|Gandikota|Andhra Pradesh|Kadapa|Rayalaseema
                lepakshi|Lepakshi|Andhra Pradesh|Sri Sathya Sai|Rayalaseema
                srisailam|Srisailam|Andhra Pradesh|Nandyal|Rayalaseema
                hyderabad|Hyderabad|Telangana|Hyderabad|Telangana Central
                warangal|Warangal|Telangana|Hanamkonda|Telangana North
                ramappa|Ramappa|Telangana|Mulugu|Telangana North
                nagarjuna-sagar|Nagarjuna Sagar|Telangana|Nalgonda|Telangana South
                bhadrachalam|Bhadrachalam|Telangana|Bhadradri Kothagudem|Telangana East
                ananthagiri-hills|Ananthagiri Hills|Telangana|Vikarabad|Telangana West
                bhubaneswar|Bhubaneswar|Odisha|Khordha|Coastal Odisha
                puri|Puri|Odisha|Puri|Coastal Odisha
                konark|Konark|Odisha|Puri|Coastal Odisha
                chilika|Chilika|Odisha|Puri|Coastal Odisha
                cuttack|Cuttack|Odisha|Cuttack|Coastal Odisha
                gopalpur|Gopalpur|Odisha|Ganjam|South Odisha
                simlipal|Simlipal|Odisha|Mayurbhanj|North Odisha
                kolkata|Kolkata|West Bengal|Kolkata|South Bengal
                darjeeling|Darjeeling|West Bengal|Darjeeling|North Bengal
                kalimpong|Kalimpong|West Bengal|Kalimpong|North Bengal
                sundarbans|Sundarbans|West Bengal|South 24 Parganas|South Bengal
                shantiniketan|Shantiniketan|West Bengal|Birbhum|South Bengal
                digha|Digha|West Bengal|Purba Medinipur|South Bengal
                bodh-gaya|Bodh Gaya|Bihar|Gaya|Magadh
                nalanda|Nalanda|Bihar|Nalanda|Magadh
                rajgir|Rajgir|Bihar|Nalanda|Magadh
                patna|Patna|Bihar|Patna|Magadh
                vaishali|Vaishali|Bihar|Vaishali|North Bihar
                amritsar|Amritsar|Punjab|Amritsar|Majha
                ludhiana|Ludhiana|Punjab|Ludhiana|Malwa
                patiala|Patiala|Punjab|Patiala|Malwa
                anandpur-sahib|Anandpur Sahib|Punjab|Rupnagar|Doaba
                srinagar|Srinagar|Jammu & Kashmir|Srinagar|Kashmir Valley
                gulmarg|Gulmarg|Jammu & Kashmir|Baramulla|Kashmir Valley
                pahalgam|Pahalgam|Jammu & Kashmir|Anantnag|Kashmir Valley
                sonamarg|Sonamarg|Jammu & Kashmir|Ganderbal|Kashmir Valley
                jammu|Jammu|Jammu & Kashmir|Jammu|Jammu Region
                patnitop|Patnitop|Jammu & Kashmir|Udhampur|Jammu Region
                leh|Leh|Ladakh|Leh|Leh
                nubra-valley|Nubra Valley|Ladakh|Leh|Nubra
                pangong-lake|Pangong Lake|Ladakh|Leh|Changthang
                tso-moriri|Tso Moriri|Ladakh|Leh|Changthang
                khardung-la|Khardung La|Ladakh|Leh|Leh
                gangtok|Gangtok|Sikkim|Gangtok|East Sikkim
                pelling|Pelling|Sikkim|Gyalshing|West Sikkim
                lachung|Lachung|Sikkim|Mangan|North Sikkim
                yumthang|Yumthang|Sikkim|Mangan|North Sikkim
                namchi|Namchi|Sikkim|Namchi|South Sikkim
                guwahati|Guwahati|Assam|Kamrup Metropolitan|Brahmaputra Valley
                kaziranga|Kaziranga|Assam|Golaghat|Upper Assam
                majuli|Majuli|Assam|Majuli|Upper Assam
                sivasagar|Sivasagar|Assam|Sivasagar|Upper Assam
                shillong|Shillong|Meghalaya|East Khasi Hills|Khasi Hills
                cherrapunji|Cherrapunji|Meghalaya|East Khasi Hills|Khasi Hills
                dawki|Dawki|Meghalaya|West Jaintia Hills|Jaintia Hills
                mawlynnong|Mawlynnong|Meghalaya|East Khasi Hills|Khasi Hills
                panaji|Panaji|Goa|North Goa|Goa
                old-goa|Old Goa|Goa|North Goa|Goa
                anjuna|Anjuna|Goa|North Goa|Goa
                palolem|Palolem|Goa|South Goa|Goa
                dudhsagar|Dudhsagar|Goa|South Goa|Goa
                delhi|Delhi|Delhi|New Delhi|National Capital Region
                varanasi|Varanasi|Uttar Pradesh|Varanasi|Purvanchal
                agra|Agra|Uttar Pradesh|Agra|Braj
                lucknow|Lucknow|Uttar Pradesh|Lucknow|Awadh
                prayagraj|Prayagraj|Uttar Pradesh|Prayagraj|Prayagraj Region
                kanpur|Kanpur|Uttar Pradesh|Kanpur Nagar|Central UP
                ayodhya|Ayodhya|Uttar Pradesh|Ayodhya|Awadh
                mathura|Mathura|Uttar Pradesh|Mathura|Braj
                vrindavan|Vrindavan|Uttar Pradesh|Mathura|Braj
                orchha-fort-belt|Orchha Fort Belt|Madhya Pradesh|Niwari|Bundelkhand
                raipur|Raipur|Chhattisgarh|Raipur|Central Chhattisgarh
                jagdalpur|Jagdalpur|Chhattisgarh|Bastar|Bastar
                ambikapur|Ambikapur|Chhattisgarh|Surguja|North Chhattisgarh
                ranchi|Ranchi|Jharkhand|Ranchi|Chotanagpur
                deoghar|Deoghar|Jharkhand|Deoghar|Santhal Pargana
                netarhat|Netarhat|Jharkhand|Latehar|Chotanagpur
                itanagar|Itanagar|Arunachal Pradesh|Papum Pare|Arunachal Foothills
                tawang|Tawang|Arunachal Pradesh|Tawang|Western Arunachal
                ziro|Ziro|Arunachal Pradesh|Lower Subansiri|Central Arunachal
                kohima|Kohima|Nagaland|Kohima|Nagaland Highlands
                mokokchung|Mokokchung|Nagaland|Mokokchung|Nagaland Highlands
                aizawl|Aizawl|Mizoram|Aizawl|Mizoram Hills
                champai|Champhai|Mizoram|Champhai|Mizoram Hills
                imphal|Imphal|Manipur|Imphal West|Imphal Valley
                loktak-lake|Loktak Lake|Manipur|Bishnupur|Imphal Valley
                agartala|Agartala|Tripura|West Tripura|Tripura Plains
                unakoti|Unakoti|Tripura|Unakoti|Tripura Hills
                """;

        List<SeedDestination> result = new ArrayList<>();
        for (String line : data.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isBlank()) continue;
            String[] parts = trimmed.split("\\|");
            if (parts.length != 5) continue;
            result.add(new SeedDestination(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim()));
        }
        return List.copyOf(result);
    }

    private record SeedDestination(String slug, String name, String state, String district, String region) {}
}
