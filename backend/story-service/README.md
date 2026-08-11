# Story Service

Cultural storytelling, historical narratives, and multi-language content for IndianRoadmap.

## Port: 8083 | MongoDB: indianroadmap_stories

## Run
```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk/26.0.2/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:/opt/homebrew/bin:$PATH

# Unit tests
./mvnw clean verify

# Start (requires destination-service on 8081)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Health
curl http://localhost:8083/actuator/health

# Swagger
open http://localhost:8083/swagger-ui.html
```

## Key APIs
- `POST /api/v1/stories` — Create story
- `GET  /api/v1/stories` — List/search
- `GET  /api/v1/stories/destination/{destinationId}` — By destination
- `POST /api/v1/stories/{id}/chapters` — Add chapter
- `POST /api/v1/stories/{id}/chapters/{chapterId}/sections` — Add section
- `POST /api/v1/stories/{id}/review` — Submit for review
- `POST /api/v1/stories/{id}/publish` — Publish
- `POST /api/v1/stories/{id}/archive` — Archive

## Environment Variables
- `MONGODB_URI` (default: mongodb://localhost:27017/indianroadmap_stories)
- `SERVER_PORT` (default: 8083)
- `DESTINATION_SERVICE_URL` (default: http://localhost:8081)
