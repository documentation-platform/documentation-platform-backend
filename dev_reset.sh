#!/bin/bash

echo "This script will remove all containers and volumes for the current project, and rebuild the project."
echo "This will result in the loss of all data in your local database."
echo "Are you sure you want to continue? (y/n)"
echo "-----------------------------------------------------"
read -r response

if [[ ! "$response" =~ ^([yY][eE][sS]|[yY])$ ]]
then
  echo "Exiting..."
  exit 1
fi

echo " "
echo "-----------------------------------------------------"
echo "Stopping and removing containers..."
echo "-----------------------------------------------------"
echo " "
docker-compose down

echo " "
echo "-----------------------------------------------------"
echo "Removing volumes..."
echo "-----------------------------------------------------"
echo " "
docker volume rm -f $(docker volume ls -q | grep mysql-data)

echo " "
echo "-----------------------------------------------------"
echo "Fetching the latest .env.example from origin/main..."
echo "-----------------------------------------------------"
echo " "
git fetch origin main
git checkout origin/main -- .env.example

rm .env
cp .env.example .env

echo " "
echo "-----------------------------------------------------"
echo "Rebuilding and starting containers... (You can stop this process with Ctrl+C)"
echo "-----------------------------------------------------"
echo " "
docker-compose up --build
