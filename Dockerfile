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

## Use an official OpenJDK runtime as a parent image
#FROM openjdk:17-jdk-slim
#
## Set the working directory inside the container
#WORKDIR /app
#
## Copy the JAR file (ensure your build outputs the JAR to the 'build/libs' directory)
#COPY build/libs/messaging-server.jar /app/messaging-server.jar
#
## Expose necessary ports
#EXPOSE 8080 7001
#
## Run the Spring Boot application
#CMD ["java", "-jar", "/app/messaging-server.jar"]
