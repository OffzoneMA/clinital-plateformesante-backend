FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
# Copy the pom.xml file to download dependencies
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline
# Copy the application source code
COPY src ./src
# Build the application
RUN mvn package -DskipTests

# Use a base image with Java and MySQL installed
FROM openjdk:17-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT  ["java", "-jar", "app.jar"]
