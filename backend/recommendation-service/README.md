# recommendation-service

IndianRoadmap Recommendation Service — deterministic, explainable, mood-based destination recommendations.

## Purpose

`recommendation-service` powers IndianRoadmap's travel discovery engine:

- Mood-based destination recommendations
- Interest and travel-style matching
- Region, duration, budget, and season matching
- Deterministic scoring with human-readable explanations
- Similar destination discovery
- Recommendation profile management

## Architecture

```
Angular
  │
  ▼
API Gateway
  │
  ▼
recommendation-service (port 8085)
  │
  ├── destination-service (port 8081)  ← live destination data
  ├── story-service (port 8083)        ← story enrichment (best-effort)
  │
  └── MongoDB (recommendation_profiles collection)
```

## Recommendation Algorithm

### Scoring Model (max 100 points)

| Factor         | Max pts | Logic                                          |
|----------------|---------|------------------------------------------------|
| Mood match     | 30      | Full=30, Partial=15, None=0                    |
| Interest match | 25      | 5pts per matching interest (max 5)             |
| Travel style   | 15      | Exact=15, Compatible=8, None=0                 |
| Region match   | 10      | Exact=10, Related=7, No preference=5, Other=0  |
| Duration match | 10      | In range=10, Close=5, Outside=0                |
| Budget match   | 5       | Within budget=5, Exceeds=0                     |
| Season match   | 5       | Matching season=5, No preference=2.5, Other=0  |

### Match Levels

| Score    | Level     |
|----------|-----------|
| 90–100   | EXCELLENT |
| 75–89    | VERY_GOOD |
| 60–74    | GOOD      |
| 40–59    | MODERATE  |
| 0–39     | LOW       |

### Mood Compatibility

Each recommendation `Mood` maps to destination-service mood values:

| Requested Mood | Primary Destination Moods    | Partial Match            |
|----------------|------------------------------|--------------------------|
| ZEN            | ZEN, SOLITUDE                | SPIRITUAL, CURIOUS       |
| ADVENTUROUS    | ADVENTURE, WILD              | CURIOUS, ZEN             |
| SPIRITUAL      | SPIRITUAL, ZEN               | HERITAGE, SOLITUDE       |
| CURIOUS        | CURIOUS, HERITAGE, WILD      | ADVENTURE, SPIRITUAL     |
| ROMANTIC       | ZEN, SOLITUDE, WILD          | SPIRITUAL, HERITAGE      |
| CULTURAL       | HERITAGE, SPIRITUAL, CURIOUS | ADVENTURE, WILD          |
| OFFBEAT        | WILD, ADVENTURE, SOLITUDE    | CURIOUS, HERITAGE        |
| SOCIAL         | HERITAGE, CURIOUS            | ADVENTURE, SPIRITUAL     |
| SOLITUDE       | SOLITUDE, ZEN, WILD          | ADVENTURE, SPIRITUAL     |
| FAMILY         | HERITAGE, SPIRITUAL, CURIOUS | WILD, PATRIOTIC          |

## Technology

- Java 26
- Spring Boot 4.1.x
- Spring Data MongoDB
- Jakarta Validation
- Spring Boot Actuator
- SpringDoc OpenAPI 3.1
- Groovy 4 + Spock 2.4
- Testcontainers
- Maven

## MongoDB

- Database: `indianroadmap_recommendations`
- Collection: `recommendation_profiles`
- Unique index on `destinationId`

## Configuration

### application.yml

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/indianroadmap_recommendations}
server:
  port: ${SERVER_PORT:8085}
indianroadmap:
  services:
    destination:
      base-url: ${DESTINATION_SERVICE_URL:http://localhost:8081}
    story:
      base-url: ${STORY_SERVICE_URL:http://localhost:8083}
```

### Environment Variables

| Variable                | Default                                                    |
|-------------------------|------------------------------------------------------------|
| `MONGODB_URI`           | `mongodb://localhost:27017/indianroadmap_recommendations`  |
| `DESTINATION_SERVICE_URL` | `http://localhost:8081`                                  |
| `STORY_SERVICE_URL`     | `http://localhost:8083`                                    |
| `SERVER_PORT`           | `8085`                                                     |

## API Documentation

### Swagger UI
```
http://localhost:8085/swagger-ui/index.html
```

### Recommendation APIs

#### Get recommendations

```http
POST /api/v1/recommendations
Content-Type: application/json

{
  "mood": "ZEN",
  "interests": ["NATURE", "SPIRITUALITY"],
  "travelStyle": "SLOW_TRAVEL",
  "durationDays": 5,
  "maxBudget": 30000,
  "preferredRegion": "HIMALAYAS",
  "season": "SUMMER",
  "limit": 5
}
```

Response:
```json
{
  "success": true,
  "data": [
    {
      "destination": { "id": "...", "slug": "adi-kailash", "name": "Adi Kailash", "region": "Kumaon" },
      "score": 91.5,
      "matchLevel": "EXCELLENT",
      "reasons": ["Perfectly matches your Zen mood", "Strong spirituality alignment"],
      "matchedMoods": ["ZEN"],
      "matchedInterests": ["SPIRITUALITY"],
      "matchedTravelStyles": ["SLOW_TRAVEL"]
    }
  ]
}
```

#### Get by mood

```http
GET /api/v1/recommendations/mood/ZEN?limit=10
```

#### Find similar destinations

```http
GET /api/v1/recommendations/destination/{slug}/similar?limit=5
```

### Profile Management APIs

```http
POST   /api/v1/recommendation-profiles
GET    /api/v1/recommendation-profiles/{destinationId}
PUT    /api/v1/recommendation-profiles/{destinationId}
DELETE /api/v1/recommendation-profiles/{destinationId}
```

## Moods

```
ZEN, ADVENTUROUS, SPIRITUAL, CURIOUS, ROMANTIC, CULTURAL, OFFBEAT, SOCIAL, SOLITUDE, FAMILY
```

## Interests

```
NATURE, MOUNTAINS, HISTORY, CULTURE, FOOD, PHOTOGRAPHY, ASTRONOMY,
SPIRITUALITY, ADVENTURE, WILDLIFE, ARCHITECTURE, LOCAL_LIFE, ROAD_TRIPS, VILLAGES
```

## Travel Styles

```
BACKPACKER, LUXURY, SLOW_TRAVEL, ROAD_TRIP, SOLO, COUPLE, FAMILY, OFFBEAT, ADVENTURE
```

## Testing

```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker)
./mvnw verify -P integration-tests
```

### Test Coverage

- `RecommendationScoringEngineSpec` — 47 tests (mood, interest, style, region, duration, budget, season, normalization)
- `RecommendationExplanationGeneratorSpec` — 11 tests
- `RecommendationServiceSpec` — 11 tests
- `RecommendationControllerSpec` — 5 tests (HTTP status, validation, sorting)
- `RecommendationRepositoryIntegrationSpec` — Testcontainers MongoDB integration

## Docker

```bash
docker build -t indianroadmap/recommendation-service .
docker run -p 8085:8085 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/indianroadmap_recommendations \
  -e DESTINATION_SERVICE_URL=http://host.docker.internal:8081 \
  indianroadmap/recommendation-service
```

## Health

```bash
curl http://localhost:8085/actuator/health
# {"status":"UP"}
```

## Future AI Compatibility

The scoring engine is abstracted behind `RecommendationEngine`:

```
Current:  RuleBasedRecommendationEngine (deterministic, explainable)
Future:   MLRecommendationEngine | LLMRecommendationEngine | HybridRecommendationEngine
```

The REST API is decoupled from the engine implementation — future AI engines can be introduced without changing the API contract.
