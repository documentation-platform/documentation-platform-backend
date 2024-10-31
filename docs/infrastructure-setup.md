# Documentation For Oracle Cloud Setup and Connection

This document outlines the steps to set up the infrastructure on Oracle Cloud and connect the instances to the MySQL database. The same principles can be applied to other cloud providers as well.

## What do we need?
- Oracle Cloud Account
- Virtual Cloud Network (VCN)
- At least 1 instance (Deployment workflow uses 2 instances)
- MySQL Database
- Load Balancer
- Oracle CLI Configuration

## Oracle Cloud Account
Sign up for an Oracle Cloud account and create a new project.

## Virtual Cloud Network (VCN)
This is a virtual cloud network that all of your resources will be connected to. You can create a VCN by following these steps:
1. Go to the Oracle Cloud Console.
2. Click on the hamburger menu on the top left.
3. Under the "Networking" section, click on "Virtual Cloud Networks".
4. Click on "Create Virtual Cloud Network".
5. Create a public and private subnet.
6. Click on "Create Virtual Cloud Network".

Note: You will need to create a public and private subnet. The public subnet will be used for the instance and the private subnet will be used for the MySQL database.

After the creation of the VCN, you will need to create a security list for the VCN. You can allow all traffic in the security list for now but you can restrict it later.

## Create an Instance
You can create an instance by following these steps:
1. Go to the Oracle Cloud Console.
2. Click on the hamburger menu on the top left.
3. Under the "Compute" section, click on "Instances".
4. Click on "Create Instance".
5. You can choose whatever shape you want for the instance. Image will want to be Ubuntu.
6. Click on "Create" and wait for the instance to be created.

## Setting up the Instance
Now that you have created your instance you will want to connect to it and set it up. You can connect to the instance by following these steps:
1. You can find the public IP address of the instance by going to the Oracle Cloud Console and clicking on the instance.
2. Copy the public IP address.
3. Open a terminal and run the following command to connect to the instance:
```bash
ssh -i [path to private key] ubuntu@[public IP address]
```
4. Once you are connected to the instance you can set it up using the setup script provided in the repository.
5. Create a file that contains the content from our `server_setup.sh` file and run the setup script by running the following command:
```bash
chmod +x server_setup.sh
sudo ./server_setup.sh
```

This will ask you for the repository link which you can choose the non-SSH one, and it will clone the repository.
6. Once the setup script is finished you can create an .env inside the `server_code` folder and fill in the necessary values.
7. Now you can restart your instance or manually start the docker container to get the server to run. Upon starting the server,
the server will be accessible at the public IP address of the instance.

## MySQL Database
You can create a MySQL database anywhere but for this project, we will be using an instance in Oracle Cloud. You can create a MySQL database by following these steps:
1. Go to the Oracle Cloud Console.
2. Click on the hamburger menu on the top left.
3. Under the "Database" section, click on "DB Systems".
4. Click on "Create DB System".
5. Choose the MySQL version you want to use.
6. Fill in the necessary details and click on "Create".

Make sure to create the MySQL database in the private subnet of the VCN. You can use the Bastion service inside the Oracle Cloud Console to connect to the MySQL database.
Since the instances are in the same VCN, they can connect to the MySQL database.

## Load Balancer
If you are creating multiple instances, you will need to create a load balancer to distribute the traffic between the instances. You can create a load balancer by following these steps:
1. Go to the Oracle Cloud Console.
2. Click on the hamburger menu on the top left.
3. Under the "Networking" section, click on "Load Balancers".
4. Click on "Create Load Balancer".
6. Add the instances to the backend set of the load balancer.
7. Configure the health check for the load balancer. You can use the health check endpoint provided in the server code. (e.g. /api/health)
8. Make sure to set the ports for the instances to the same port that the server is running on (e.g. 8080).
9. You can change the Interval in milliseconds to 5000 so that the health check is done every 5 seconds. (Default is 10000)
10. Click on "Create".

Now all the instances will be connected to the load balancer and the load balancer will distribute the traffic between the instances. When
pinging the servers use the load balancer IP address.

## Oracle CLI Configuration
You will want to configure Oracle CLI so that we can talk to our resources from the Github workflows. You can configure Oracle CLI by following these steps:
1. You can go into your profile and create an API key.
2. This API keys configuration will be used to configure the Oracle CLI.
3. After setting up the API Key you can put that information into the repository secrets.
4. (Optional) If you want to use the CLI without downloading it you can press on the command line icon in the Oracle Cloud Console and use the CLI from there.
