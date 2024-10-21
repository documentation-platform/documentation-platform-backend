# Documentation For Oracle Cloud setup and connection

## Create VCN
  Setup private and public subnet
  Created instance (in the public subnet for now makes setup easier)
  DB heatwave option setup within private subnet
  Add internet gateway 0.0.0.0/0 with all default settings
  
## Setup Ubuntu server
ubuntu@instance:~$ nano serversetup.sh
ubuntu@instance:~$ chmod +x serversetup.sh
ubuntu@instance:~$ sudo ./serversetup.sh  

## Connection to Ubuntu server
Run this command in ubuntu terminal to connect to VCN:
ssh -i [filename with oracle ssh] ubuntu@129.146.82.26

## Running Springboot Project
./server_update.sh (goated script)


