# Stage 1: Build
FROM eclipse-temurin:26-jdk AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:26-jre
WORKDIR /app

RUN addgroup --system recapp && adduser --system --group recapp
COPY --from=builder /app/target/*.jar app.jar
RUN chown recapp:recapp app.jar

USER recapp

ENV MONGODB_URI=mongodb://localhost:27017/indianroadmap_recommendations \
    DESTINATION_SERVICE_URL=http://localhost:8081 \
    STORY_SERVICE_URL=http://localhost:8083 \
    SERVER_PORT=8085

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
