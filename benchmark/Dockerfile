# Use an official OpenJDK runtime as a parent image
FROM amazoncorretto:17-alpine3.19

# Set the working directory inside the container
WORKDIR /app

# Copy the application JAR file into the container
COPY target/benchmark-*.jar benchmark.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/benchmark.jar"]
