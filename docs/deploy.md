# Server Deployment Guide

This guide explains the GitHub Actions workflow for deploying to a server, such as an EC2 instance, but you can use any Ubuntu server for your deployment. The setup process can be adapted to various environments, not just AWS.

## Disclaimer: Database Setup

**Important:** Before deploying your application, you need to set up a MySQL database separately. This guide does not cover the database setup process.

You can use [Amazon RDS for MySQL](https://aws.amazon.com/rds/mysql/) for a managed database solution. After setting up your database, make sure to add the following environment variables to your EC2 instance:

- `MYSQL_HOST`: The endpoint of your RDS instance
- `MYSQL_PORT`: The port number (usually 3306 for MySQL)
- `MYSQL_DATABASE`: The name of your database
- `MYSQL_USER`: The username for accessing the database
- `MYSQL_PASSWORD`: The password for the database user

These variables should be used in your application's configuration to connect to the database.

## GitHub Actions Workflow

```yaml
name: Deploy to EC2

on:
  push:
    branches:
      - master

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to EC2
        env:
          PRIVATE_KEY: ${{ secrets.EC2_SSH_PRIVATE_KEY }}
          HOST: ${{ secrets.EC2_HOST }}
          USER: ${{ secrets.EC2_USER }}
        run: |
          echo "$PRIVATE_KEY" > private_key && chmod 600 private_key
          ssh -o StrictHostKeyChecking=no -i private_key ${USER}@${HOST} '
            cd spring-boot-mysql-docker-template &&
            git pull origin master &&
            sudo docker compose -f compose.prod.yaml up --build -d &&
            echo "Deployment commands executed successfully"
          '
      - name: Wait for deployment
        run: |
          echo "Waiting for 45 seconds to allow the application to start..."
          sleep 45
      - name: Check deployment
        env:
          HOST: ${{ secrets.EC2_HOST }}
          API_ENDPOINT: ${{ secrets.API_ENDPOINT }}
        run: |
          response=$(curl -s -o /dev/null -w "%{http_code}" http://${HOST}:${API_ENDPOINT}/api/health)
          if [ $response = "200" ]; then
            echo "Deployment successful!"
          else
            echo "Deployment failed!"
            exit 1
          fi
```

## Workflow Explanation

1. **Trigger**: The workflow runs when changes are pushed to the main branch.
2. **Deployment**:
    - Uses SSH to connect to the server
    - Pulls the latest code from the master branch
    - Rebuilds and restarts Docker containers using docker-compose
3. **Verification**:
    - Waits 45 seconds for the application to start
    - Checks if the application is running by making a request to a health endpoint

## AWS Setup Instructions

1. **Launch an EC2 instance**:
    - Go to AWS EC2 dashboard and click "Launch Instance"
    - Choose an Ubuntu Image
    - Select an instance type (t2.micro is often sufficient for small projects)
    - Configure instance details, add storage, and configure security group
    - Create or select an existing key pair for SSH access

2. **Configure Security Group**:
    - Allow inbound traffic on port 22 for SSH
    - Allow inbound traffic on your application port (e.g., 8080 for Spring Boot)

3. **Set up the EC2 instance**:
    - SSH into your instance
    - Install Docker and Docker Compose
    - Clone your GitHub repository
    - Set up necessary environment variables

4. **Configure GitHub Secrets**:
   In your GitHub repository settings, add these secrets:
    - `EC2_SSH_PRIVATE_KEY`: The private key for SSH access
    - `EC2_HOST`: Public DNS or IP of your EC2 instance
    - `EC2_USER`: Username for SSH (usually 'ec2-user' for Amazon Linux)
    - `API_ENDPOINT`: The port your application runs on

5. **Push to Main Branch or Run Manually**:
    - ```sudo docker compose -f compose.prod.yaml up --build```

6. **Domain Configuration (Optional)**:
    - Use Route 53 to point your domain to the EC2 instance

7. **Monitoring Setup (Recommended)**:
    - Use AWS CloudWatch for monitoring and alerts

## Best Practices

- Secure your EC2 instance and keep GitHub secrets safe
- Follow AWS best practices for production deployments
- Consider setting up staging environments
- Implement comprehensive testing before production deployment

This setup enables continuous deployment, automatically updating your production environment with the latest code whenever you push to the master branch.