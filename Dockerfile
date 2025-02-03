FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Gradle Wrapper files
COPY gradlew gradlew
COPY gradle gradle

# Make gradlew executable inside the container
RUN chmod +x gradlew

# Copy the build files
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

# Build the application
RUN ./gradlew build --no-daemon

# Copy the built JAR file
COPY build/libs/*.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8080
