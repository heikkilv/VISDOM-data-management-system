# MongoDB for the Data Management System

## Installing MongoDB for the data management system

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

- Edit file [`.env`](.env) (port numbers, admin username and password, etc.). By default no authentication is used with MongoDB.
    - the environment file contains explanations about the available setting variables
- Run command: `./start_mongo.sh`

## Uninstalling MongoDB

- Run command: `./stop_mongo.sh`

## Removing the Mongo data

- Run command: `./remove_data.sh`
    - works only if MongoDB is not running
