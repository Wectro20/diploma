# Start with a base image containing Java runtime
FROM openjdk:17-oracle

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 6969 available to the world outside this container
EXPOSE 6969

# Copy the jar file into the container at /app.jar
COPY build/libs/*.jar diploma-0.0.1-SNAPSHOT.jar

# Run the jar file
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/diploma-0.0.1-SNAPSHOT.jar"]

