# IndianRoadmap Application Startup Guide

This guide starts the full stack locally (backend microservices + API gateway + Angular frontend).

## 1. Prerequisites

From project root:

```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0
```

Set Java 26 and shared JWT secret:

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk/26.0.2/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:/opt/homebrew/bin:$PATH
export JWT_SECRET=indianroadmap-super-secret-jwt-key-2026-production-ready
```

Ensure MongoDB is running on:

```text
localhost:27017
```

## 2. Start backend services (one terminal per service)

### destination-service (8081)
```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0/backend/destination-service
./mvnw spring-boot:run
```

### roadmap-service (8082)
```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0/backend/roadmap-service
./mvnw spring-boot:run
```

### story-service (8083)
```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0/backend/story-service
./mvnw spring-boot:run
```

### audio-service (8084)
```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0/backend/audio-service
./mvnw spring-boot:run
```

### recommendation-service (8085)
```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0/backend/recommendation-service
./mvnw spring-boot:run
```

### user-service (8086)
```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0/backend/user-service
./mvnw spring-boot:run
```

### ai-service (8090)
```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0/backend/ai-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python main.py
```

### api-gateway (8080) — start last
```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0/backend/api-gateway
./mvnw spring-boot:run
```

## 3. Start Angular frontend

```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0/frontend/indianroadmap-web
npm install
npm start
```

URLs:

- Frontend: `http://localhost:4200`
- Gateway: `http://localhost:8080`

Angular must call only the gateway (not ports 8081–8086 directly).

## 4. Health checks

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
curl http://localhost:8086/actuator/health
curl http://localhost:8090/actuator/health
```

## 5. Smoke test through gateway

### Public destination API
```bash
curl "http://localhost:8080/api/v1/destinations?size=3"
```

### Register
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"TestPassword1234!","firstName":"Demo","lastName":"User","displayName":"Demo"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"TestPassword1234!"}'
```

Use returned `accessToken` for protected endpoints:

```bash
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <accessToken>"
```

### AI mood recommendations
```bash
curl -X POST http://localhost:8080/api/ai/recommendations/mood \
  -H "Content-Type: application/json" \
  -d '{"mood":"SPIRITUAL","durationDays":6,"budget":"MID_RANGE"}'
```

## 6. Docker Compose option (one command)

If Docker is available:

```bash
cd /Users/aditibodkhe/IdeaProjects/IndianRoadmap-1.0
cp .env.example .env
docker compose up --build -d
./docker/scripts/health-check.sh
```

Stop services:

```bash
docker compose down
```
