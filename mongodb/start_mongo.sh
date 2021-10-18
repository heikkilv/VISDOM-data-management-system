#!/bin/bash

env_file=".env"
volume_variable="MONGODB_VOLUME"
network_variable="MONGODB_NETWORK"

# assumes that the names do not contain the character '='
volume_name=$(cat $env_file | grep $volume_variable= | cut -d '=' -f 2)
network_name=$(cat $env_file | grep $network_variable= | cut -d '=' -f 2)

# create the external Docker volume for the MongoDB data if it does not exist
docker volume inspect $volume_name >/dev/null 2>&1 || docker volume create $volume_name

# create the external Docker network for the Data Management System components if it does not exist
docker network inspect $network_name >/dev/null 2>&1 || docker network create $network_name

# start the MongoDB and Mongo Express containers
docker-compose up --detach
