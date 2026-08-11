# IndianRoadmap

An immersive Indian travel discovery and cultural storytelling platform.

---

## Architecture

```
Angular (future)
      │
      ▼
API Gateway :8080   ← single public entry point
      │
      ├── user-service             :8086
      ├── destination-service      :8081
      ├── roadmap-service          :8082
      ├── story-service            :8083
      ├── audio-service            :8084
      └── recommendation-service  :8085
                  │
               MongoDB :27017
```

---

## Services

| Service                 | Port | Database                         | Description                          |
|-------------------------|------|----------------------------------|--------------------------------------|
| api-gateway             | 8080 | —                                | JWT auth, routing, CORS, rate-limit  |
| destination-service     | 8081 | indianroadmap_destinations       | Destinations, coordinates, elevation |
| roadmap-service         | 8082 | indianroadmap_roadmaps           | Roadmaps, nodes, edges, routes       |
| story-service           | 8083 | indianroadmap_stories            | Stories, chapters, multilingual      |
| audio-service           | 8084 | indianroadmap_audio              | TTS audio generation (MOCK)          |
| recommendation-service  | 8085 | indianroadmap_recommendations    | Mood-based recommendations           |
| user-service            | 8086 | indianroadmap_users              | Registration, JWT, refresh tokens    |

---

## Prerequisites

- Docker Desktop 4.x+ with Compose v2
- 8 GB RAM recommended
- 4 CPU cores recommended

---

## Quick Start

### 1. Create your `.env` file

```bash
cp .env.example .env
```

Edit `.env` and set `JWT_SECRET` to a secure random string (minimum 32 characters):

```
JWT_SECRET=your-very-long-random-secret-value-here-32chars
```

### 2. Start the entire backend

```bash
docker compose up --build -d
```

The first build downloads JDK images and compiles all services — this takes ~10 minutes.
Subsequent starts with cached layers take ~2 minutes.

### 3. Verify everything is healthy

```bash
docker compose ps
```

All containers should show `(healthy)` status.

Run the health check script:

```bash
./docker/scripts/health-check.sh
```

### 4. Gateway health endpoint

```bash
curl http://localhost:8080/actuator/health
```

Expected:
```json
{"status":"UP"}
```

---

## Common Commands

| Command                            | Description                         |
|------------------------------------|-------------------------------------|
| `docker compose up --build -d`     | Build and start all services        |
| `docker compose up -d`             | Start with existing images          |
| `docker compose down`              | Stop all services, keep data        |
| `docker compose down -v`           | Stop and delete all volumes (reset) |
| `docker compose ps`                | Show container status               |
| `docker compose logs -f`           | Tail all logs                       |
| `docker compose logs -f api-gateway` | Tail gateway logs only            |
| `docker compose restart api-gateway` | Restart one service               |

---

## Environment Variables

Copy `.env.example` to `.env`. All variables have sensible defaults except `JWT_SECRET`.

| Variable                   | Default                           | Description                              |
|----------------------------|-----------------------------------|------------------------------------------|
| `JWT_SECRET`               | *(required)*                      | HS256 signing key — min 32 chars         |
| `MONGODB_PORT`             | `27017`                           | MongoDB host port (for local tooling)    |
| `CORS_ALLOWED_ORIGINS`     | `http://localhost:4200`           | Comma-separated Angular origins          |
| `GATEWAY_CONNECT_TIMEOUT_MS` | `3000`                          | Gateway upstream connect timeout         |
| `GATEWAY_RESPONSE_TIMEOUT_MS` | `10000`                        | Gateway upstream response timeout        |
| `JWT_ACCESS_EXPIRATION`    | `900`                             | Access token TTL in seconds (15 min)     |
| `JWT_REFRESH_EXPIRATION`   | `2592000`                         | Refresh token TTL in seconds (30 days)   |

Service URLs inside Docker network (`*_SERVICE_URL`) default to the Docker service names and should not be changed unless containers are renamed.

---

## API Gateway

The Angular frontend connects **only** to the gateway:

```
http://localhost:8080
```

### Route Table

| Path prefix                          | Backend service         | Auth required |
|--------------------------------------|-------------------------|---------------|
| `POST /api/v1/auth/register`         | user-service            | Public        |
| `POST /api/v1/auth/login`            | user-service            | Public        |
| `POST /api/v1/auth/refresh`          | user-service            | Public        |
| `GET  /api/v1/destinations/**`       | destination-service     | Public        |
| `GET  /api/v1/stories/**`            | story-service           | Public        |
| `GET  /api/v1/roadmaps/**`           | roadmap-service         | Public        |
| `POST/PUT/DELETE /api/v1/destinations/**` | destination-service | ADMIN        |
| `POST/PUT/DELETE /api/v1/stories/**` | story-service           | ADMIN         |
| `GET /api/v1/users/me`               | user-service            | Authenticated |
| `POST /api/v1/recommendations`       | recommendation-service  | Authenticated |
| `GET  /api/v1/recommendations/**`    | recommendation-service  | Authenticated |
| `/api/v1/audio/**`                   | audio-service           | Authenticated |

### Swagger UI (per service)

| Service                | URL                                         |
|------------------------|---------------------------------------------|
| api-gateway            | http://localhost:8080/swagger-ui/index.html |
| destination-service    | http://localhost:8081/swagger-ui.html       |
| roadmap-service        | http://localhost:8082/swagger-ui.html       |
| story-service          | http://localhost:8083/swagger-ui.html       |
| audio-service          | http://localhost:8084/swagger-ui.html       |
| recommendation-service | http://localhost:8085/swagger-ui.html       |
| user-service           | http://localhost:8086/swagger-ui.html       |

---

## Authentication Flow

```
POST /api/v1/auth/register  →  create account
POST /api/v1/auth/login     →  { accessToken, refreshToken }

GET  /api/v1/users/me
     Authorization: Bearer <accessToken>

POST /api/v1/auth/refresh
     { refreshToken }        →  { accessToken, refreshToken }  (rotation)

POST /api/v1/auth/logout
     Authorization: Bearer <accessToken>  →  revoke refresh token
```

### JWT Headers forwarded downstream

After validation the gateway strips any client-supplied values and adds:

```
X-User-Id:   <userId>
X-User-Role: <USER|ADMIN|CONTENT_EDITOR>
```

---

## MongoDB

One MongoDB container is shared; each service owns its own database:

```
indianroadmap_destinations
indianroadmap_roadmaps
indianroadmap_stories
indianroadmap_audio
indianroadmap_recommendations
indianroadmap_users
```

Data is persisted in the `mongodb_data` Docker volume.

Audio files are persisted in the `audio_data` Docker volume at `/app/data/audio`.

Connect with MongoDB Compass at `mongodb://localhost:27017` (host-bound port).

---

## Security Model

- The API Gateway is the **only** service exposed on the host network (`:8080`).
- All microservices communicate via the private `indianroadmap-network` Docker network.
- MongoDB is bound to `127.0.0.1:27017` (local access only).
- JWT secrets are never committed to source control — use `.env`.
- Internal services currently trust `X-User-Id`/`X-User-Role` headers added by the gateway. A service-to-service authentication layer (e.g. mTLS or a shared service token) should be added before production deployment.

---

## Troubleshooting

### Port already in use

```
Error: bind: address already in use  (port 8080)
```

Find and stop the conflicting process:

```bash
lsof -i :8080
kill <PID>
```

Or change the gateway port in `docker-compose.yml` `ports:` section.

### MongoDB not starting

```bash
docker compose logs mongodb
```

If the data volume is corrupted:

```bash
docker compose down -v
docker compose up -d
```

### Service shows `unhealthy`

```bash
docker compose logs <service-name>
```

Common causes: MongoDB not yet ready (health check retries handle this), missing `JWT_SECRET`, incorrect service URL.

### Gateway cannot reach a service

```bash
docker compose exec api-gateway wget -qO- http://destination-service:8081/actuator/health
```

If the service name is not resolved, verify both containers are on `indianroadmap-network`:

```bash
docker network inspect indianroadmap-network
```

### JWT authentication failure (401)

- Check `JWT_SECRET` is the same value in `user-service` and `api-gateway` (both read `JWT_SECRET` env var).
- Verify the token is sent as `Authorization: Bearer <token>`.
- Access tokens expire after 15 minutes by default — use refresh token to get a new one.

### CORS failure

- Add your frontend origin to `CORS_ALLOWED_ORIGINS` in `.env` (comma-separated).
- Restart: `docker compose restart api-gateway`.

### Docker image build failure

- Ensure Docker has internet access (JDK image must be pulled).
- Try: `docker compose build --no-cache <service-name>`.

### MongoDB connection failure inside container

Services connect via: `mongodb://mongodb:27017/<database>`. Ensure:
1. `mongodb` container is healthy: `docker compose ps mongodb`
2. The service is on `indianroadmap-network`

---

## Resetting the environment

```bash
# Stop and remove containers + volumes (all data lost)
docker compose down -v

# Fresh start
docker compose up --build -d
```

---

## Local Development (without Docker)

Each service can run independently against a local MongoDB:

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk/26.0.2/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
export JWT_SECRET=indianroadmap-super-secret-jwt-key-2026-production-ready

cd backend/user-service
./mvnw spring-boot:run
```

Service URLs default to `localhost` when `*_SERVICE_URL` env vars are not set.
