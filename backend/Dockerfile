# Build stage
FROM gradle:8.10-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :adapter-web:bootJar --no-daemon -q

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/adapter-web/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
