FROM maven:3.9.6-eclipse-temurin-21-alpine

WORKDIR /app

# Copy pom.xml first (for dependency caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Copy properties file
COPY src/main/resources/application.properties ./src/main/resources/application.properties

EXPOSE 8080

# Run directly with Maven (no JAR needed)
ENTRYPOINT ["mvn", "spring-boot:run"]