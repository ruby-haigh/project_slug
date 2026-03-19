# Importing JDK and copying required files
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application (lighter image)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/*.jar app.jar

# Run the app
ENTRYPOINT ["java","-jar","app.jar"]

EXPOSE 8080