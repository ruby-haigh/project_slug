# Stage 1: Build the application
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven

# Copy your project files
COPY pom.xml .
COPY src/ src/

# Build the app
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Run the app
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8080