# Server Deployment Guide

This guide explains the GitHub Actions workflow for deploying to multiple servers and checking the deployment status using a health check endpoint. You can adapt this setup to deploy to multiple instances in different environments. 

## Disclaimer: Database Setup

**Important:** Before deploying your application, you need to set up a MySQL database separately. This guide does not cover the database setup process.

You can use [Amazon RDS for MySQL](https://aws.amazon.com/rds/mysql/) or another provider for a managed database solution. After setting up your database, make sure to add the following environment variables to your instance:

- `MYSQL_HOST`: The endpoint of your RDS instance
- `MYSQL_PORT`: The port number (usually 3306 for MySQL)
- `MYSQL_DATABASE`: The name of your database
- `MYSQL_USER`: The username for accessing the database
- `MYSQL_PASSWORD`: The password for the database user

These variables should be used in your application's configuration to connect to the database.

## GitHub Actions Workflow

```yaml
name: Deploy to Instances

on:
   push:
      branches:
         - main

jobs:
   deploy:
      runs-on: ubuntu-latest
      env:
         OCI_CLI_USER: ${{ secrets.OCI_CLI_USER }}
         OCI_CLI_TENANCY: ${{ secrets.OCI_CLI_TENANCY }}
         OCI_CLI_FINGERPRINT: ${{ secrets.OCI_CLI_FINGERPRINT }}
         OCI_CLI_KEY_CONTENT: ${{ secrets.OCI_CLI_KEY_CONTENT }}
         OCI_CLI_REGION: ${{ secrets.OCI_CLI_REGION }}
         API_URL: ${{ secrets.API_URL }}

      steps:
         - name: Restart Server 1 (Instance 1)
           uses: oracle-actions/run-oci-cli-command@v1.1.1
           id: restart_instance_1
           with:
              command: "compute instance action --instance-id ${{ secrets.OCI_INSTANCE_ONE_OCID }} --action SOFTRESET"

         - name: Allow time for server to restart
           run: sleep 300

         - name: Check Server 1 Health
           uses: oracle-actions/run-oci-cli-command@v1.1.1
           id: check_instance_1_health
           with:
              command: "lb backend-health get --backend-name ${{ secrets.OCI_INSTANCE_ONE_PRIVATE_IP_AND_PORT }} --backend-set-name ${{ secrets.OCI_LB_BACKEND_SET }} --load-balancer-id ${{ secrets.OCI_LB_OCID }}"

         - name: Verify Server 1 Health Status
           run: |
              OUTPUT='${{ steps.check_instance_1_health.outputs.output }}'

              # Parse the status from data object
              HEALTH_STATUS=$(echo "$OUTPUT" | jq -r 'fromjson | .data.status')
              echo "Health Status: $HEALTH_STATUS"
              if [ "$HEALTH_STATUS" != "OK" ]; then
                echo "Server 1 health check failed! Status: $HEALTH_STATUS"
                exit 1
              fi

         - name: Restart Server 2 (Instance 2)
           uses: oracle-actions/run-oci-cli-command@v1.1.1
           id: restart_instance_2
           with:
              command: "compute instance action --instance-id ${{ secrets.OCI_INSTANCE_TWO_OCID }} --action SOFTRESET"

         - name: Allow time for server to restart
           run: sleep 300

         - name: Check Server 2 Health
           uses: oracle-actions/run-oci-cli-command@v1.1.1
           id: check_instance_2_health
           with:
              command: "lb backend-health get --backend-name ${{ secrets.OCI_INSTANCE_TWO_PRIVATE_IP_AND_PORT }} --backend-set-name ${{ secrets.OCI_LB_BACKEND_SET }} --load-balancer-id ${{ secrets.OCI_LB_OCID }}"

         - name: Verify Server 2 Health Status
           run: |
              OUTPUT='${{ steps.check_instance_2_health.outputs.output }}'

              # Parse the status from data object
              HEALTH_STATUS=$(echo "$OUTPUT" | jq -r 'fromjson | .data.status')
              echo "Health Status: $HEALTH_STATUS"
              if [ "$HEALTH_STATUS" != "OK" ]; then
                echo "Server 2 health check failed! Status: $HEALTH_STATUS"
                exit 1
              fi

         - name: Check deployment
           run: |
              response=$(curl -s -o /dev/null -w "%{http_code}" ${API_URL}/api/health)
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
    - Restarts the first instance using the Oracle Cloud CLI
    - Waits for the server to restart
    - Checks the health status of the first instance
    - Restarts the second instance
    - Waits for the server to restart
    - Checks the health status of the second instance
    - Verifies the health status of both instances
    - Checks the deployment status by making a request to a health endpoint

## General Setup Instructions

1. **Launch at least two instances**: 
    - Go to Oracle Cloud Console and create two instances.
    - Note down the OCID of each instance.
2. **Create a Load Balancer**:
    - Create a load balancer in Oracle Cloud Console.
    - Add the instances to the backend set of the load balancer.
    - Note down the OCID of the load balancer.
3. **Set up Oracle Cloud CLI**:
    - Configure the CLI with the required credentials.
4. **Set up GitHub Secrets**:
    - In your GitHub repository settings, add these secrets:
        - `OCI_CLI_USER`: Oracle Cloud CLI user
        - `OCI_CLI_TENANCY`: Oracle Cloud tenancy OCID
        - `OCI_CLI_FINGERPRINT`: Fingerprint of the public key used for CLI
        - `OCI_CLI_KEY_CONTENT`: Private key content for CLI
        - `OCI_CLI_REGION`: Oracle Cloud region
        - `OCI_INSTANCE_ONE_OCID`: OCID of the first instance
        - `OCI_INSTANCE_TWO_OCID`: OCID of the second instance
        - `OCI_INSTANCE_ONE_PRIVATE_IP_AND_PORT`: Private IP and port of the first instance
        - `OCI_INSTANCE_TWO_PRIVATE_IP_AND_PORT`: Private IP and port of the second instance
        - `OCI_LB_OCID`: OCID of the load balancer
        - `OCI_LB_BACKEND_SET`: Name of the backend set in the load balancer
        - `API_URL`: The URL of your API. This is used to check if the deployment was successful
5. **Push to Main Branch or Run Manually**:
    - Push your changes to the main branch to trigger the deployment workflow.

For further instructions on setting up the servers in oracle, you can refer to our setup markdown file [here](infrastructure-setup).

## Best Practices

- Secure your instance and keep GitHub secrets safe
- Follow best practices for production deployments
- Consider setting up staging environments
- Implement comprehensive testing before production deployment

This setup enables continuous deployment, automatically updating your production environment with the latest code whenever you push to the master branch.