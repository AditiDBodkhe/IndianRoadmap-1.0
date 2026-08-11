# Stage 1: Build
FROM eclipse-temurin:26-jdk AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src/ src/
RUN ./mvnw clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:26-jre
WORKDIR /app
RUN groupadd -r appuser && useradd -r -g appuser appuser
COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:appuser app.jar
USER appuser
EXPOSE 8081
ENV JAVA_OPTS="-XX:+UseZGC -XX:+UseStringDeduplication"
ENTRYPOINT ["java", "-jar", "app.jar"]
