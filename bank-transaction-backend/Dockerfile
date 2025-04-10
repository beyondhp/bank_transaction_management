# Use lightweight OpenJDK for production
FROM openjdk:21-jre-slim

# Set working directory
WORKDIR /app

# Copy the JAR file built by Maven
COPY target/transaction-management-1.0-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 