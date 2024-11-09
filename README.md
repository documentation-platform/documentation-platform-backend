# MarkDock API Repo
This repository stores all the code for the MarkDock API.

## Documentation 

- [Testing Guide](docs/testing.md)
- [Migration Guide](docs/migration.md)
- [Single Instance Deployment Guide (Previous Strategy)](docs/single-instance-deploy.md)
- [Multiple Instance Deployment Guide (Current Strategy)](docs/multiple-instance-deploy.md)
- [Infrastructure Setup Guide](docs/infrastructure-setup.md)
- [Docker Guide](docs/docker.md)
- [GitHub Workflows ](docs/github-workflows.md)

## Features

- Spring Boot application
- MySQL database
- Flyway for database migrations
- Docker setup for both development and production environments
- GitHub Actions for running tests and deployment to a server

## Table of Contents

- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)

## Prerequisites

- Docker and Docker Compose
- Git

## Getting Started

1. Clone this repository:
   ```
   git clone git@github.com:mark-dock/mark-dock-api.git
   ```

2. Set up environment variables:
   Copy the `.env.example` file to `.env` and fill in the necessary values.

3. Start the development environment:
   ```
   docker compose up 
   ```

4. Access the application at `http://127.0.0.1:8080` (Port 8080 will be replaced by the value specified in the environment variable {SPRING_APP_PORT}."
5. To check the server's health, visit: `http://127.0.0.1:8080/api/health`

6. (Optional) To enable automatic reloading of changes in Spring Boot, the configuration is already set up in the project. However, you'll need to adjust some permissions in your editor. For IntelliJ, follow these steps:

- Go to **Settings > Build, Execution, Deployment > Compiler** and enable **Build project automatically**.
- Then, go to **Advanced Settings** and check **Allow auto-make to start even if the developed application is currently running**.


## Project Structure
The main project structure is as follows:

- `.github/` - GitHub Actions workflows
- `docs/` - Documentation files
- `src/` - Source code
- `migrations/` - Flyway database migrations
- `pom.xml` - Maven dependencies

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

Tests will automatically run on pull requests.
