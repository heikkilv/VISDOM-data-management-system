# Data broker

<!-- no toc -->
- [Environment variables for the data broker](#environment-variables-for-the-data-broker)
    - [Variables related to the MongoDB connection](#variables-related-to-the-mongodb-connection)
    - [Variables related to the deployment of the data broker](#variables-related-to-the-deployment-of-the-data-broker)
- [Installing data broker](#installing-data-broker)
    - [Requirements](#requirements)
    - [Installation instructions](#installation-instructions)
- [API for the data broker](#api-for-the-data-broker)
    - [`/adapters` endpoint](#adapters-endpoint)
    - [`/fetchers` endpoint](#fetchers-endpoint)
    - [`/info` endpoint](#info-endpoint)
    - [`/swagger` endpoint](#swagger-endpoint)
- [Notes about using data broker](#notes-about-using-data-broker)
- [Uninstalling data broker](#uninstalling-data-broker)

The data broker can be used to fetch information about the available data adapters and data fetchers.

## Environment variables for the data broker

Before starting the data broker, the deployer must modify the environment variables. The file [`.env`](.env) contains a template for setting up the variables and some comments about what each variable is used for. All variables are also introduced in this section.

### Variables related to the MongoDB connection

the username and the password can be left empty if the used MongoDB instance does not use access control
| Variable name                | Default value  | Description                                                                  |
| ---------------------------- | -------------- | ---------------------------------------------------------------------------- |
| `MONGODB_HOST`               | visdom-mongodb | Mongo host name (either Docker container name or address to the Mongo host)  |
| `MONGODB_PORT`               | 27017          | MongoDB port number                                                          |
| `MONGODB_NETWORK`            | visdom-network | Docker network name for the MongoDB (needed if MongoDB is only available from a private Docker network) |
| `MONGODB_METADATA_DATABASE`  | metadata       | The database that is used to store the metadata about the data adapter (the same metadata database should be used for all the components in the data management system) |
| `MONGODB_USERNAME`           |                | MongoDB username (must have read/write permission for the metadata database) |
| `MONGODB_PASSWORD`           |                | MongoDB password (must have read/write permission for the metadata database) |

### Variables related to the deployment of the data broker

| Variable name      | Default value | Description                                                            |
| ------------------ | ------------- | ---------------------------------------------------------------------- |
| `APPLICATION_NAME` | visdom-broker | The data broker name, also used as the Docker container name           |
| `HOST_NAME`        | localhost     | The host server name (only used for generating the Swagger definition) |
| `HOST_PORT`        | 8765          | The host server port number for the data broker API                    |

## Installing data broker

### Requirements

- Docker: [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)
- Docker Compose: [https://docs.docker.com/compose/install/](https://docs.docker.com/compose/install/)
- Running MongoDB instance: see [../../../mongodb](../../../mongodb) for instructions on how to setup MongoDB

The environment used in testing:

- Ubuntu 18.04
- GNU Bash, version 4.4.20(1)-release
- Docker, version 20.10.6
- docker-compose, version 1.28.2

### Installation instructions

1. Edit the environmental variable file [`.env`](.env)
2. Run the command: `docker-compose up --detach`

The API for the data broker will be available at the port defined by `HOST_PORT` on the host machine.

The Swagger UI interface will be available at the address: `http://HOST_NAME:HOST_PORT/swagger`

## API for the data broker

| Endpoint    | Description                                    |
| ----------- | ---------------------------------------------- |
| `/adapters` | Returns information about active data adapters |
| `/fetchers` | Returns information about active data fetchers |
| `/info`     | Returns information about the data broker      |
| `/swagger`  | Swagger UI interface for the API               |

### `/adapters` endpoint

The endpoint returns a status code 200 and JSON formatted list containing information about all the active data adapters. The adapter information contains the adapter name, type and API address along with some other metadata.

### `/fetchers` endpoint

The endpoint returns a status code 200 and JSON formatted list containing information about all the active data fetchers. The fetcher information contains the fetcher name, type and API address along with some other metadata.

### `/info` endpoint

The endpoint returns a status code 200 and JSON formatted object containing information about the data broker.

### `/swagger` endpoint

The data broker provides a Swagger UI interface to test the data broker API.

## Notes about using data broker

- No access control for data broker has been implemented.
- A metadata document will be added/updated by the data broker at startup and every 5 minutes. This metadata document is located at the metadata collection in the metadata database in MongoDB.
- For developers, no proper unit tests have been implemented for the GitLab data fetcher.

## Uninstalling data broker

Run the command: `docker-compose down`
