#!/bin/bash
# Copyright 2022 Tampere University
# This software was developed as a part of the VISDOM project: https://iteavisdom.org/
# This source code is licensed under the MIT license. See LICENSE in the repository root directory.
# Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

env_file=".env"
worker_number_variable="SPARK_WORKERS"
worker_name_variable="SPARK_WORKER_BASE_NAME"
master_port_variable="SPARK_MASTER_UI_PORT"
network_variable="SPARK_NETWORK"

# assumes that the names do not contain the character '='
workers=$(cat $env_file | grep $worker_number_variable= | cut --delimiter='=' --fields 2)
worker_name_base=$(cat $env_file | grep $worker_name_variable= | cut --delimiter='=' --fields 2)
worker_port_base=$(cat $env_file | grep $master_port_variable= | cut --delimiter='=' --fields 2)
network_name=$(cat $env_file | grep $network_variable= | cut --delimiter='=' --fields 2)

# create a new docker-compose file from the master, worker and network parts
cp docker-compose-master.yml docker-compose.yml

worker_number=1
while [ $worker_number -le $workers ]
do
    worker_port=$((worker_port_base + worker_number))
    sed -e "s/SPARK_WORKER_NUMBER/$worker_number/g" \
        -e "s/SPARK_WORKER_UI_PORT/$worker_port/g" docker-compose-worker.yml >> docker-compose.yml
    worker_number=$((worker_number + 1));
done

cat docker-compose-network.yml >> docker-compose.yml

# create the external Docker network for the Data Management System components if it does not exist
docker network inspect $network_name >/dev/null 2>&1 || docker network create $network_name

# start the containers for the Spark master and the Spark workers
docker-compose up --detach
