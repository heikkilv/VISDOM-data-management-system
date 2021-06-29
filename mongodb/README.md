# MongoDB for the Data Management System

<!-- no toc -->
- [Installing MongoDB](#installing-mongodb)
    - [Requirements](#requirements)
    - [Installation instructions](#installation-instructions)
- [Uninstalling MongoDB](#uninstalling-mongodb)
- [Removing the Mongo data](#removing-the-mongo-data)

[MongoDB](https://www.mongodb.com/) is a general purpose document database.
In the VISDOM data management system it is used to store all the information fetched from the software tools.

## Installing MongoDB

### Requirements

- Bash
- Docker: [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)
- Docker Compose: [https://docs.docker.com/compose/install/](https://docs.docker.com/compose/install/)

The environment used in testing:

- Ubuntu 18.04
- GNU Bash, version 4.4.20(1)-release
- Docker, version 20.10.6
- docker-compose, version 1.28.2

### Installation instructions

It is possible to use MongoDB without any access control by leaving the root user name and password empty (the default values).

The start script will also deploy Mongo Express that can be used for browsing the stored Mongo data using a web browser.

Installation steps:

1. Edit the environmental variable file [`.env`](.env) (port numbers, admin username and password, etc.). By default no authentication is used with MongoDB.
    - the environment file contains explanations about the available variables
2. Run the command: `./start_mongo.sh`

The MongoDB will be available at the port defined by `MONGODB_PORT` (by default: `27017`) in the Docker network defined by `MONGODB_NETWORK` (default name: `visdom-network`).

The Mongo express will be available at the port defined by `MONGO_EXPRESS_PORT` (by default: `8801`) on the host server.

## Uninstalling MongoDB

- Run the command: `./stop_mongo.sh`

## Removing the Mongo data

- Run the command: `./remove_data.sh`
    - works only if MongoDB is not running
