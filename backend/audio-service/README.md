# Audio Service

IndianRoadmap Audio Service — Text-to-Speech narration and audio metadata management.

## Port: 8084 | MongoDB: indianroadmap_audio

## Architecture

```
story-service (port 8083)
       │
       │ section content
       ▼
audio-service (port 8084)
       │
   ┌───┴───────────┐
   ▼               ▼
TtsProvider    AudioStorage
(Mock/Google)  (Local/S3 future)
       │
       ▼
  MongoDB metadata
```

## Technology
- Java 26
- Spring Boot 4.1.x
- Spring Data MongoDB
- Jakarta Validation
- OpenAPI / Swagger
- Groovy + Spock + Testcontainers

## Environment Variables

| Variable             | Default                                       | Description                    |
|----------------------|-----------------------------------------------|--------------------------------|
| `MONGODB_URI`        | `mongodb://localhost:27017/indianroadmap_audio` | MongoDB connection             |
| `SERVER_PORT`        | `8084`                                        | HTTP port                      |
| `STORY_SERVICE_URL`  | `http://localhost:8083`                       | story-service base URL         |
| `AUDIO_STORAGE_PATH` | `./data/audio`                                | Local audio file storage       |
| `TTS_PROVIDER`       | `MOCK`                                        | TTS provider (MOCK/GOOGLE/...)  |

## Run

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk/26.0.2/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:/opt/homebrew/bin:$PATH

# Tests
./mvnw clean verify

# Start with local profile (requires story-service on 8083)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Docker

```bash
docker build -t indianroadmap/audio-service .
docker run -p 8084:8084 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/indianroadmap_audio \
  -e STORY_SERVICE_URL=http://host.docker.internal:8083 \
  indianroadmap/audio-service
```

## Swagger
```
http://localhost:8084/swagger-ui/index.html
```

## Health
```
http://localhost:8084/actuator/health
```

## API

### Generate Audio
```http
POST /api/v1/audio
Content-Type: application/json

{
  "storyId": "story-id",
  "chapterId": "chapter-id",
  "sectionId": "section-id",
  "language": "ENGLISH",
  "voiceName": "default",
  "voiceGender": "NEUTRAL",
  "format": "MP3"
}
```

### Get Audio
```http
GET /api/v1/audio/{audioId}
```

### Download Audio File
```http
GET /api/v1/audio/{audioId}/content
```

### List Audio for Story
```http
GET /api/v1/audio/story/{storyId}?language=ENGLISH&status=COMPLETED&page=0&size=20
```

### List Audio for Section
```http
GET /api/v1/audio/section/{sectionId}?language=HINDI
```

### Regenerate Audio
```http
POST /api/v1/audio/{audioId}/regenerate
Content-Type: application/json

{
  "voiceName": "female-voice",
  "voiceGender": "FEMALE",
  "format": "MP3"
}
```

### Delete Audio
```http
DELETE /api/v1/audio/{audioId}
```

## TTS Provider Abstraction

The service uses `TtsProvider` interface. Currently only `MockTtsProvider` is implemented.

To add a real provider (e.g. Google):
1. Implement `TtsProvider`
2. Add case to `TtsProviderFactory`
3. Set `TTS_PROVIDER=GOOGLE` environment variable

No changes to `AudioServiceImpl` are required.
