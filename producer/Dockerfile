# Use an official OpenJDK runtime as a parent image
FROM amazoncorretto:17-alpine3.19

# Set the working directory inside the container
WORKDIR /app

# Copy the application JAR file into the container
COPY target/producer-*.jar producer.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/producer.jar"]
