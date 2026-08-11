# User Service

Authentication, JWT issuance, refresh-token rotation, profile management, and user preferences for IndianRoadmap.

## Port: 8086 | MongoDB: indianroadmap_users

## Run
```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk/26.0.2/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:/opt/homebrew/bin:$PATH
export JWT_SECRET=dev-secret-key-at-least-32-chars-long-for-hs256

./mvnw clean test
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
curl http://localhost:8086/actuator/health
open http://localhost:8086/swagger-ui/index.html
```

## Key APIs
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/users/me`
- `PUT /api/v1/users/me`
- `GET /api/v1/users/me/preferences`
- `PUT /api/v1/users/me/preferences`

## Environment Variables
- `MONGODB_URI` (default: `mongodb://localhost:27017/indianroadmap_users`)
- `SERVER_PORT` (default: `8086`)
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION` (default: `900`)
- `JWT_REFRESH_EXPIRATION` (default: `2592000`)
