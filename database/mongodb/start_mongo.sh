#!/bin/bash

docker volume create visdom-mongodb-data
docker-compose up --detach
