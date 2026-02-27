FROM maven:3.9.6-eclipse-temurin-21-alpine

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

EXPOSE 8080

ENTRYPOINT ["mvn", "spring-boot:run", "-Dspring-boot.run.profiles=default", "-Dspring-boot.run.jvmArguments=-Dserver.port=${PORT:-10000} -Ddebug"]