# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Debug: List what's in target folder
RUN ls -la /app/target/

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Debug: Verify jar is copied
RUN ls -la /app/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]