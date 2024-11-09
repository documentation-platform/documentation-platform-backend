# GitHub Workflow
This project uses GitHub Actions to automate the testing and deployment processes. The workflows are defined in the `.github/workflows` directory.

This explains the GitHub Actions used inside the project and how they work.

## Testing Workflow
- **File**: `.github/workflows/run_tests.yml`
- **Trigger**: This workflow runs on every pull request to main.

This workflow runs all the tests inside this project. If you create any tests inside `src/test/java`, they will be executed by this workflow and required to pass
on every pull request.

## Deployment Workflow
- **File**: `.github/workflows/deployment.yml`
- **Trigger**: This workflow runs when changes are pushed to the main branch.

This workflow deploys the application to the server.

## Build and Health Check Workflow
- **File**: `.github/workflows/build_check.yml`
- **Trigger**: This workflow runs on every pull request to main.

This workflow builds the application and runs a health check on the server. This is to ensure that the application is running correctly before deploying it to the server.

## Migration Validation Workflow
- **File**: `.github/workflows/migration_validation.yml`
- **Trigger**: This workflow runs on every pull request to main.

This workflow checks any migrations inside the pull request. It checks the versioning of the migration to make sure its not behind
any existing migrations in the database. It also verifies that the migration naming convention is correct and is not in the future compared to the current date.

## Deployment In-Progress Workflow
- **File**: `.github/workflows/deployment_progress_check.yml`
- **Trigger**: This workflow runs on every pull request to main.

This workflow checks if there is a deployment in progress. If there is a deployment in progress, it will fail the pull request and notify the user that there is a deployment in progress.
If your pull request fails because of this workflow, please wait for the deployment to finish and then re-run the workflow inside the pull request.