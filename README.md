# Spring Boot Backend Template

This repository serves as a perfect template for starting a backend server with [Spring Boot](https://spring.io/projects/spring-boot), [MySQL](https://www.mysql.com/) database, [Flyway](https://flywaydb.org/) for migrations, and [Docker](https://www.docker.com/) for containerization. It also includes GitHub Actions for CI/CD and deployment to an Ubuntu server (Using [AWS EC2](https://aws.amazon.com/ec2/) as an example).

## Operating System Compatibility

**Important:** This template is primarily designed and tested for Ubuntu. If you're using Windows, you can use Windows Subsystem for Linux (WSL) for a smoother experience. If you choose to use Windows natively, minor tweaks might be needed throughout the setup and deployment process.

## Documentation 

- [Testing Guide](docs/testing.md)
- [Migration Guide](docs/migration.md)
- [Deployment Guide](docs/deploy.md)
- [Docker Guide](docs/docker.md)

## Features

- Spring Boot application
- MySQL database
- Flyway for database migrations
- Docker setup for both development and production environments
- GitHub Actions for CI/CD
- Automated deployment to a server (using AWS EC2 as an example)

## Table of Contents

- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)

## Prerequisites

- Docker and Docker Compose
- Git

## Getting Started

1. Clone this repository:
   ```
   git clone https://github.com/brezden/spring-boot-mysql-docker-template.git
   cd spring-boot-mysql-docker-template
   ```

2. Set up environment variables:
   Copy the `.env.example` file to `.env` and fill in the necessary values.

3. Start the development environment:
   ```
   docker compose up 
   ```

4. Access the application at `http://127.0.0.1:8080` (Port 8080 will be replaced by the value specified in the environment variable {SPRING_APP_PORT}."
5. To check the server's health, visit: `http://127.0.0.1:8080/api/health` 

## Project Structure

```
├── .github/
│   └── workflows/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│
├── migrations/
│
├── .mvn/
│
├── compose.prod.yaml
├── compose.yaml
├── Dockerfile
├── Dockerfile.pipeline
├── .env
├── .env.example
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Development

For local development, use the `compose.yaml` file:

```bash
docker compose up
```

This will start both the MySQL database and the Spring Boot application in development mode.

## Testing

Run tests using Maven:

```bash
./mvnw test
```

CI/CD will automatically run tests for non-main branches.

## Deployment

The project includes GitHub Actions workflows for:

1. CI/CD on all branches
2. Deployment to server for the main branch

See the `.github/workflows/` directory for details.
