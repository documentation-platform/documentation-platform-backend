#!/bin/bash

# Update the system
sudo DEBIAN_FRONTEND=noninteractive apt-get update
sudo DEBIAN_FRONTEND=noninteractive apt-get upgrade -y

# Install Docker
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" -y
sudo DEBIAN_FRONTEND=noninteractive apt-get update
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y docker-ce

# Start Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Add current user to the docker group
sudo usermod -aG docker $USER

# Log out and back in for group changes to take effect
echo "Please log out and back in for the group changes to take effect."

# Clone GitHub repository if it doesn't exist
read -p "Enter your repository URL: " repo_url
git clone "$repo_url" server_code

mv server_code/server_update.sh ~
chmod +x server_update.sh

# Create a systemd service to run the startup script
SERVICE_FILE="/etc/systemd/system/docker-start.service"
cat << EOF | sudo tee $SERVICE_FILE
[Unit]
Description=Run Docker Update Script Once on Boot
After=docker.service
Requires=docker.service

[Service]
Type=oneshot
User=ubuntu
ExecStart=/home/ubuntu/server_update.sh
WorkingDirectory=/home/ubuntu/server_code
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
EOF

# Reload systemd and enable the service
sudo systemctl daemon-reload
sudo systemctl enable docker-start.service

#Gives permission for Github to pull the repo
sudo chown -R ubuntu:ubuntu /home/ubuntu/server_code
echo "Setup complete! The server will now pull the latest code and start the Docker containers on startup."
