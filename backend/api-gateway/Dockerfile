# Stage 1: Build
FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src/ src/
RUN ./mvnw clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
RUN addgroup -S gateway && adduser -S gateway -G gateway
COPY --from=builder /app/target/*.jar app.jar
RUN chown gateway:gateway app.jar
USER gateway
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
