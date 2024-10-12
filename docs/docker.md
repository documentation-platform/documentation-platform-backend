# Docker Setup Documentation

This document outlines the Docker configuration for development, production, and CI/CD environments.

**Disclaimer:** All variable values (MYSQL_USER, MYSQL_DATABASE etc.) are pulled from your environment file (.env). Ensure you have properly configured the .env files for each environment (development, production) with the appropriate values.
This document outlines the Docker configuration for development, production, and CI/CD environments.

## Development Environment

The development environment uses Docker Compose with two services: MySQL and Spring Boot application.

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: mysql
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    ports:
      - "${MYSQL_PORT}:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  spring-app:
    image: openjdk:21-jdk-slim
    container_name: spring-boot
    volumes:
      - ./:/volume_code
    working_dir: /volume_code
    command: bash -c "chmod +x mvnw && ./mvnw spring-boot:run"
    ports:
      - "${SPRING_APP_PORT}:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      SPRING_PROFILES_ACTIVE: DEV
      SPRING_FLYWAY_LOCATIONS: filesystem:migrations
    depends_on:
      mysql:
        condition: service_healthy

volumes:
  mysql-data:
```

### Key Features:
- MySQL 8.0 database with health check
- Spring Boot application using OpenJDK 21
- Volume mapping for immediate code reflection without rebuilding
- Environment variables for database configuration
- Flyway migrations support

## Production Environment

The production environment uses a simplified Docker Compose setup:

```yaml
services:
  spring-app:
    build:
      context: .
      dockerfile: Dockerfile.prod
    container_name: spring-boot-prod
    ports:
      - "${SPRING_APP_PORT}:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://${MYSQL_ENDPOINT}:${MYSQL_PORT}/${MYSQL_DATABASE}
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      SPRING_PROFILES_ACTIVE: PROD
      SPRING_FLYWAY_LOCATIONS: classpath:db/migrations
```

### Key Features:
- Custom-built Spring Boot application
- Environment variables for external MySQL database connection
- Production profile activation

## Dockerfile for Production

The production Dockerfile uses a multi-stage build process:

```dockerfile
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
```

### Key Features:
- Multi-stage build for optimized image size
- Builds the application in the first stage
- Runs the application in a slim JDK image in the second stage

## CI/CD Dockerfile

The CI/CD Dockerfile is used in GitHub Actions for running tests:

```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY . /app

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

CMD ["./mvnw", "test"]
```

### Key Features:
- Uses OpenJDK 21 slim image
- Copies the project files into the container
- Downloads dependencies offline
- Runs Maven tests as the default command

This setup provides a comprehensive Docker environment for development, production, and CI/CD processes, ensuring consistency across different stages of the application lifecycle.