# Running Tests on Deployments Guide

This guide explains the GitHub Actions workflow for running tests.

## GitHub Actions Workflow

```yaml
name: Run Tests

on:
   push:
      branches:
         - '*'

jobs:
  test-and-lint:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: rootpassword
          MYSQL_DATABASE: project_db
          MYSQL_USER: username
          MYSQL_PASSWORD: password
        ports:
          - 3306:3306
        options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=3
    
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up Docker Build
        uses: docker/setup-buildx-action@v1
      
      - name: Build and run tests in Docker
        env:
          SPRING_APP_PORT: 8080
          MYSQL_DATABASE: project_db
          MYSQL_USER: username
          MYSQL_PASSWORD: password
        run: |
          docker build -t spring-app-test -f Dockerfile.pipeline .
          docker run --rm \
            --network host \
            -e SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/project_db \
            -e SPRING_DATASOURCE_USERNAME=username \
            -e SPRING_DATASOURCE_PASSWORD=password \
            -e SPRING_PROFILES_ACTIVE=TEST \
            -e SPRING_FLYWAY_LOCATIONS=filesystem:migrations \
            spring-app-test \
            ./mvnw test
```

## Workflow Explanation

1. **Trigger**: The workflow runs on every push and pull request to any branch.

2. **Environment**:
    - Uses Ubuntu as the runner OS
    - Sets up a MySQL service container for testing

3. **Steps**:
    - Checkout the code
    - Set up Docker Buildx
    - Build a Docker image for testing
    - Run tests inside the Docker container

## Key Components

1. **MySQL Service**:
    - Uses MySQL 8.0
    - Sets up a test database with credentials
    - Includes a health check to ensure MySQL is ready before tests run

2. **Docker Build**:
    - Uses a separate `Dockerfile.pipeline` for testing
    - Builds an image named `spring-app-test`

3. **Test Execution**:
    - Runs in a Docker container
    - Uses host network to connect to MySQL
    - Sets necessary environment variables
    - Executes tests using Maven

## Best Practices

- Write thorough unit and integration tests
- Monitor test execution times and optimize when necessary
- Use code coverage tools to ensure adequate test coverage

This ensures that all code changes are tested automatically, helping to catch issues early in the development process.