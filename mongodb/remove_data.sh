#!/bin/bash
# Copyright 2022 Tampere University
# This software was developed as a part of the VISDOM project: https://iteavisdom.org/
# This source code is licensed under the MIT license. See LICENSE in the repository root directory.
# Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

env_file=".env"
volume_variable="MONGODB_VOLUME"
network_variable="MONGODB_NETWORK"

# assumes that the names do not contain the character '='
volume_name=$(cat $env_file | grep $volume_variable= | cut --delimiter='=' --fields 2)
network_name=$(cat $env_file | grep $network_variable= | cut --delimiter='=' --fields 2)

read -p "Do you want to remove the Docker volume '$volume_name' and network '$network_name'? [y/N] " answer
if [[ -z "$answer" ]] || [[ "$answer" != "y" ]]
then
    return 0 2> /dev/null || exit 0
fi

# remove the external Docker volume containing MongoDB data if it exists
echo "Removing volume '$volume_name'"
docker volume inspect $volume_name >/dev/null 2>&1 && docker volume rm $volume_name

# remove the external Docker network for the Data Management System components if it exists
echo "Removing network '$network_name'"
docker network inspect $network_name >/dev/null 2>&1 && docker network rm $network_name
