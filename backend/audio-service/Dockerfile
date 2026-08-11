FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src/ src/
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
RUN addgroup -S audio && adduser -S audio -G audio
COPY --from=builder /app/target/*.jar app.jar
USER audio
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
