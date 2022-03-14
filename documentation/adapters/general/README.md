# General data model adapter

<!-- no toc -->
- [General data model description](#general-data-model-description)
- [Environment variables for the general data model adapter](#environment-variables-for-the-general-data-model-adapter)
    - [Variables related to the MongoDB connection](#variables-related-to-the-mongodb-connection)
    - [Variables related to the Apache Spark connection](#variables-related-to-the-apache-spark-connection)
    - [Variables related to the deployment of the general data model adapter](#variables-related-to-the-deployment-of-the-general-data-model-adapter)
- [Installing general data model adapter](#installing-general-data-model-adapter)
    - [Requirements](#requirements)
    - [Installation instructions](#installation-instructions)
- [API for the general data model adapter](#api-for-the-general-data-model-adapter)
    - [Query parameters for multi type query](#query-parameters-for-multi-type-query)
    - [Example response from multi type query](#example-response-from-multi-type-query)
    - [Query parameters for single type query](#query-parameters-for-single-type-query)
    - [Example response from single type query](#example-response-from-single-type-query)
        - [Example GitLab Origin](#example-gitlab-origin)
        - [Example commit event](#example-commit-event)
    - [Info endpoint](#info-endpoint)
    - [Filter query syntax](#filter-query-syntax)
- [Notes about using general data model adapter](#notes-about-using-general-data-model-adapter)
- [Uninstalling GitLab data adapter](#uninstalling-gitlab-data-adapter)

The general data model adapter can be used to fetch software data given in a general data model.
A diagram describing the used data model can be seen below:
![Diagram of general software data model](general-data-model.png)

The main objects of the data model are events, constructs and origins. A construct is an aspect of software engineering that can be either metadata (i.e. static data) or an artifact that can have different states. An event represents some kind of action that has happened to one or more constructs. Each event has an author that can be represented as an artifact. Both events and construct have an origin which represents the source and context for the events and constructs.

The adapter uses [Apache Spark](https://spark.apache.org/) to handle the raw data and to modify it the general data model. The current implementation supports raw data from GitLab (commits, files, pipelines). Implementation for support for A+ data (course metadata, submissions, points) is in progress. See section [General data model description](#general-data-model-description) for the implementation details regarding the data model of the supported raw data.

## General data model description

TODO

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
- Running Apache Spark instance: [Apache Spark for the Data Management System](../../../spark) for instructions on how to setup Apache Spark

Additional requirements to be able to use general data model adapter:

- Running MongoDB instance: see [MongoDB for the Data Management System](../../../mongodb) for instructions on how to setup MongoDB.
- At least one of the following:
    - Raw GitLab data stored in the MongoDB. See [GitLab fetcher](../../fetchers/gitlab) for instructions on how to setup and use the GitLab data fetcher.
    - Raw A+ data stored in the MongoDB. See [A+ fetcher](../../fetchers/aplus) for instructions on how to setup and use the A+ data fetcher.

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

## API for the general data model adapter

For getting data from the adapter there are several endpoints that can output multiple objects with a single query. Each type of object has its own endpoint, `origins`, `events`, `artifacts`, `authors`, and `metadata`. The `single` endpoint is for getting a single object based on its id and type.

TODO: add some information about the update and info endpoints

| Endpoint     | Type   | Description |
| ------------ | ------ | ----------- |
| `/origins`   | multi  | Returns origin objects |
| `/events`    | multi  | Returns events objects |
| `/authors`   | multi  | Returns author objects |
| `/artifacts` | multi  | Returns non-author artifact objects |
| `/metadata`  | multi  | Returns metadata objects |
| `/single`    | single | Returns a single object |
| `/update`    | cache  | Updates the cache to make other queries faster |
| `/info`      | info   | Returns general adapter information |

In addition, the `/swagger` endpoint provides a Swagger UI interface to test the adapter API. It also contains some examples about possible responses in different problem cases. The corresponding [swagger.json](./swagger.json) definition can also be found out in this folder.

### Query parameters for multi type query

Returns the origin objects corresponding to the query parameters.

| Query parameter | Type | Default | Description |
| --------------- | ---- | ------- | ----------- |
| `page`     | optional | 1   | The page number used in filtering the results. |
| `pageSize` | optional | 100 | The page size used in filtering the results. Maximum allowed value is 10000. |
| `type`     | optional |     | The type of the origin objects. Empty value indicates that all origin types are considered. |
| `query`    | optional |     | Filter query to be applied. See section [Filter query syntax](#filter-query-syntax) for the allowed syntax. |
| `data`     | optional |     | A comma-separated list of data attributes included in the response. Empty value indicates that all data attributes will be included. |
| `links`    | optional |     | Indicates what type object links are included in the results. Empty value indicates that all links are included. Allowed values: all, events, constructs, none |

The links parameter is not available for the `/origins` endpoint.

### Example response from multi type query

Successful query returns a JSON response with a status code 200. The format of the response is the following:

```json
{
    "count": 10,
    "page": 1,
    "page_size": 10,
    "next_page": 2,
    "previous_page": null,
    "total_count": 25,
    "results": [
        // 10 objects given as a list
    ]
}
```

where `count` is the number of results in the response, `page` and `page_size` are the used pagination options, `next_page` and `previous_page` are the page numbers for the next and previous set of results (with a value null if there are no more results in that particular direction), and `total_count` is the total number results matching the query parameters.

### Query parameters for single type query

TODO: add a description to the single query type.

| Query parameter | Type     | Description                  |
| --------------- | ----     | ---------------------------- |
| `type`          | required | The type of the object.      |
| `uuid`          | required | The unique id of the object. |

### Example response from single type query

If an object matching the query parameters was found, then a response with status code 200 and the corresponding object represented in JSON is returned.

#### Example GitLab Origin

TODO: add a proper example

```json
{
}
```

#### Example commit event

TODO: add a proper example

```json
{
}
```

TODO: add examples for other types of supported object types

### Info endpoint

The `/info` endpoint does not take any query parameters and it returns a status code 200 and JSON formatted object containing information about the general data model adapter.

Example output:

```json
{
    "adapterType": "GeneralModel",
    "apiAddress": "http://visdom-adapter-general:8080",
    "componentName": "adapter",
    "componentType": "visdom-adapter-general",
    "startTime": "2022-03-14T09:26:28.980Z",
    "swaggerDefinition": "/api-docs/swagger.json",
    "version": "0.1"
}
```

### Filter query syntax

TODO

## Notes about using general data model adapter

TODO: add at least information on how the cache in the adapter works

## Uninstalling general data model adapter

Run the command: `docker-compose down`
