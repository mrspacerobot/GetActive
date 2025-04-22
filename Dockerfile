# Use a single stage build for simplicity
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy just what's needed
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Build the application
RUN ./gradlew build -x test

# Run the application
ENTRYPOINT ["java", "-jar", "build/libs/GetActive-1.0-SNAPSHOT.jar"]