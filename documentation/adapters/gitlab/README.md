# GitLab data adapter

The GitLab data adapter can be used to receive structured data based on the raw GitLab data stored in MongoDB. The adapter uses [Apache Spark](https://spark.apache.org/) to handle the raw data.

## Environment variables for the GitLab data adapter

Before starting the GitLab data adapter, the deployer must modify the environment variables. The file [`.env`](.env) contains a template for setting up the variables and some comments about what each variable is used for. All variables are also introduced in this section.

### Variables related to the MongoDB connection

the username and the password can be left empty if the used MongoDB instance does not use access control
| Variable name                | Default value  | Description                                                                 |
| ---------------------------- | -------------- | --------------------------------------------------------------------------- |
| `MONGODB_HOST`               | visdom-mongodb | Mongo host name (either Docker container name or address to the Mongo host) |
| `MONGODB_PORT`               | 27017          | MongoDB port number                                                         |
| `MONGODB_METADATA_DATABASE`  | metadata       | The database that is used to store the metadata about the data adapter (the same metadata database should be used for all the components in the data management system) |
| `MONGODB_DATA_DATABASE`      | gitlab         | The database where the raw Gitlab data is stored                            |
| `MONGODB_USERNAME`           |                | MongoDB username (must have read/write permission for the 2 databases)      |
| `MONGODB_PASSWORD`           |                | MongoDB password (must have read/write permission for the 2 databases)      |

### Variables related to the Apache Spark connection

| Variable name       | Default value       | Description                                                                 |
| ------------------- | ------------------- | --------------------------------------------------------------------------- |
| `SPARK_MASTER_HOST` | visdom-spark-master | The host name for the Spark master (the port number is expected to be 7077) |
| `SPARK_PORT`        | 4140                | Port number for the Spark client API                                        |

### Variables related to the deployment of the GitLab data adapter

| Variable name      | Default value         | Description                                                            |
| ------------------ | --------------------- | ---------------------------------------------------------------------- |
| `APPLICATION_NAME` | visdom-adapter-gitlab | The GitLab adapter name, also used as the Docker container name        |
| `HOST_NAME`        | localhost             | The host server name (only used for generating the Swagger definition) |
| `HOST_PORT`        | 9876                  | The host server port number for the GitLab adapter API                 |
| `ADAPTER_NETWORK`  | visdom-network        | Docker network name (for accessing Spark and MongoDB)                  |

## Installing GitLab data adapter

### Requirements

Installation requirements:

- Docker: [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)
- Docker Compose: [https://docs.docker.com/compose/install/](https://docs.docker.com/compose/install/)
- Running Apache Spark instance: [../../../spark](../../../spark) for instructions on how to setup Apache Spark

Additional requirements to be able to use GitLab data adapter:

- Running MongoDB instance: see [../../../mongodb](../../../mongodb) for instructions on how to setup MongoDB
- Raw GitLab data stored in MongoDB. See [../../fetchers/gitlab](../../fetchers/gitlab) for instructions on how to setup and use the GitLab data fetcher

The environment used in testing:

- Ubuntu 18.04
- GNU Bash, version 4.4.20(1)-release
- Docker, version 20.10.6
- docker-compose, version 1.28.2

### Installation instructions

1. Edit the environmental variable file [`.env`](.env)
2. Run the command: `docker-compose up --detach`

The API for the GitLab data adapter will be available at the port defined by `HOST_PORT` on the host machine.

The Swagger UI interface will be available at the address: `http://HOST_NAME:HOST_PORT/swagger`

## API for the GitLab data adapter

| Endpoint      | Description                                                               |
| ------------- | ------------------------------------------------------------------------- |
| `/commits`    | Returns the number of commits per day and user for the GitLab projects    |
| `/timestamps` | Returns the commit timestamps for the asked files for each GitLab project |
| `/info`       | information about the GitLab data adapter                                 |
| `/swagger`    | Swagger UI interface for the API                                          |

### `/commits` endpoint

Returns the number of commits per day and user for the GitLab projects.

| Query parameter | Type     | Description                                                                                              |
| --------------- | -------- | -------------------------------------------------------------------------------------------------------- |
| `projectName`   | optional | if given only data for the given GitLab project is returned, otherwise data for all projects is returned |
| `userName`      | optional | if given only data for the given user is returned, otherwise data for all users is returned              |
| `startDate`     | optional | the earliest date for the results given in ISO 8601 format, default: no limit                            |
| `endDate`       | optional | the latest date for the results given in ISO 8601 format, default: no limit                              |

Successful query returns a JSON response with a status code 200. If there is a problem with the query, the returned status code will be either 400, 404 or 500 depending on the problem. See the Swagger API definition for more details.

The stored raw data in MongoDB is expected to be stored in the collection `commits` in the database defined by the `MONGODB_DATA_DATABASE` environment variable.

### `/timestamps` endpoint

Returns the timestamps for the commits that have changed the files or folders provided in the query for the GitLab projects.

| Query parameter | Type     | Description                                                                                              |
| --------------- | -------- | -------------------------------------------------------------------------------------------------------- |
| `filePaths`     | required | a comma-separated list of file (or folder) names                                                         |
| `projectName`   | optional | if given only data for the given GitLab project is returned, otherwise data for all projects is returned |
| `startDate`     | optional | the earliest considered timestamp given in ISO 8601 format with timezone                                 |
| `endDate`       | optional | the latest considered timestamp given in ISO 8601 format with timezone                                   |

Successful query returns a JSON response with a status code 200. If there is a problem with the query, the returned status code will be either 400, 404 or 500 depending on the problem. See the Swagger API definition for more details.

The stored raw data in MongoDB is expected to be stored in the collections `commits` and `files` in the database defined by the `MONGODB_DATA_DATABASE` environment variable.

### `/info` endpoint

The endpoint returns a status code 200 and JSON formatted object containing information about the GitLab data adapter.

### `/swagger` endpoint

The GitLab data adapter provides a Swagger UI interface to test the GitLab data adapter API. It also contains some examples about possible responses in different problem cases.

## Notes about using GitLab data adapter

- No limits on how many queries can be start concurrently have been implemented.
- No access control for GitLab data adapter has been implemented.
- Projects in GitLab have both an id number and a name. Both of these can be used as the `project_name` attribute when using the API. However, it is recommended that only the name is used, since this is the assumed way the GitLab data fetcher stores the raw data.
- No proper input checking has been implemented for the API.
- A metadata document will be added/updated by the GitLab data adapter at startup and every 5 minutes. This metadata document is located at the metadata collection in the metadata database in MongoDB.
- For developers, no proper unit tests have been implemented for the GitLab data adapter.

## Uninstalling GitLab data adapter

Run the command: `docker-compose down`
