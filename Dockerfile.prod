# Stage 1: Build
FROM openjdk:21-jdk AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn ./.mvn
COPY mvnw .
COPY src ./src
COPY migrations ./src/main/resources/db/migrations/

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=build /app/target/spring-boot-prod.jar /app/target/spring-boot-prod.jar

ENTRYPOINT ["java", "-jar", "/app/target/spring-boot-prod.jar"]

EXPOSE 8080
