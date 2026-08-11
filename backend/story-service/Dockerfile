FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src/ src/
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
RUN addgroup -S story && adduser -S story -G story
COPY --from=builder /app/target/*.jar app.jar
USER story
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
