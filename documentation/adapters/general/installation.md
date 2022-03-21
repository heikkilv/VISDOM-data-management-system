# General data model adapter deployment

<!-- no toc -->
- [Environment variables for the general data model adapter](#environment-variables-for-the-general-data-model-adapter)
    - [Variables related to the MongoDB connection](#variables-related-to-the-mongodb-connection)
    - [Variables related to the Apache Spark connection](#variables-related-to-the-apache-spark-connection)
    - [Variables related to the deployment of the general data model adapter](#variables-related-to-the-deployment-of-the-general-data-model-adapter)
- [Installing general data model adapter](#installing-general-data-model-adapter)
    - [Requirements](#requirements)
    - [Installation instructions](#installation-instructions)
- [Notes about using general data model adapter](#notes-about-using-general-data-model-adapter)
- [Uninstalling general data model adapter](#uninstalling-general-data-model-adapter)

The adapter uses [Apache Spark](https://spark.apache.org/) to handle the raw data and to modify it the general data model. The current implementation supports raw data from GitLab (commits, files, pipelines). Implementation for support for A+ data (course metadata, submissions, points) is in progress.

## Environment variables for the general data model adapter

Before starting the general data model adapter, the deployer must modify the environment variables. The file [`.env`](.env) contains a template for setting up the variables and some comments about what each variable is used for. All variables are also introduced in this section.

### Variables related to the MongoDB connection

the username and the password can be left empty if the used MongoDB instance does not use access control
| Variable name               | Default value  | Description                                                                 |
| --------------------------- | -------------- | --------------------------------------------------------------------------- |
| `MONGODB_HOST`              | visdom-mongodb | Mongo host name (either Docker container name or address to the Mongo host) |
| `MONGODB_PORT`              | 27017          | MongoDB port number |
| `MONGODB_METADATA_DATABASE` | metadata       | The database that is used to store the metadata about the data adapter (the same metadata database should be used for all the components in the data management system) |
| `GITLAB_DATABASE`           | gitlab         | The database where the raw Gitlab data is stored (read permission required) |
| `APLUS_DATABASE`            | aplus          | The database where the raw A+ data is stored (read permission required) |
| `CACHE_DATABASE`            | cache          | The database that is used as cache to make querying results faster (write permission required) |
| `MONGODB_USERNAME`          |                | MongoDB username (read permissions required for raw data and write permissions to cache and metadata) |
| `MONGODB_PASSWORD`          |                | MongoDB password (read permissions required for raw data and write permissions to cache and metadata) |

### Variables related to the Apache Spark connection

| Variable name       | Default value       | Description                                                                 |
| ------------------- | ------------------- | --------------------------------------------------------------------------- |
| `SPARK_MASTER_HOST` | visdom-spark-master | The host name for the Spark master (the port number is expected to be 7077) |
| `SPARK_PORT`        | 4140                | Port number for the Spark client API                                        |

### Variables related to the deployment of the general data model adapter

| Variable name      | Default value         | Description                                                            |
| ------------------ | --------------------- | ---------------------------------------------------------------------- |
| `APPLICATION_NAME` | visdom-adapter-gitlab | The GitLab adapter name, also used as the Docker container name        |
| `HOST_NAME`        | localhost             | The host server name (only used for generating the Swagger definition) |
| `HOST_PORT`        | 8222                  | The host server port number for the GitLab adapter API                 |
| `ADAPTER_NETWORK`  | visdom-network        | Docker network name (for accessing Spark and MongoDB)                  |

## Installing general data model adapter

### Requirements

Installation requirements:

- Docker: [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)
- Docker Compose: [https://docs.docker.com/compose/install/](https://docs.docker.com/compose/install/)
- Running Apache Spark instance: [Apache Spark for the Data Management System](../../../spark/README.md) for instructions on how to setup Apache Spark

Additional requirements to be able to use general data model adapter:

- Running MongoDB instance: see [MongoDB for the Data Management System](../../../mongodb/README.md) for instructions on how to setup MongoDB.
- At least one of the following:
    - Raw GitLab data stored in the MongoDB. See [GitLab fetcher](../../fetchers/gitlab/README.md) for instructions on how to setup and use the GitLab data fetcher.
    - Raw A+ data stored in the MongoDB. See [A+ fetcher](../../fetchers/aplus/README.md) for instructions on how to setup and use the A+ data fetcher.

The environment used in testing:

- Ubuntu 18.04
- GNU Bash, version 4.4.20(1)-release
- Docker, version 20.10.6
- docker-compose, version 1.28.2

### Installation instructions

1. Edit the environmental variable file [`.env`](.env)
2. Run the command: `docker-compose up --detach`

The API for the general data model adapter will be available at the port defined by `HOST_PORT` on the host machine.

The Swagger UI interface will be available at the address: `http://HOST_NAME:HOST_PORT/swagger`

The web page that can be used to see the running and completed Spark jobs will be available at the port defined by `SPARK_PORT` on the host machine.

## Notes about using general data model adapter

TODO

## Uninstalling general data model adapter

Run the command: `docker-compose down`
