# GitLab data fetcher

<!-- no toc -->
- [Environment variables for the GitLab data fetcher](#environment-variables-for-the-gitlab-data-fetcher)
    - [Variables related to the deployment of the GitLab data fetcher](#variables-related-to-the-deployment-of-the-gitlab-data-fetcher)
    - [Variables related to the GitLab server connection](#variables-related-to-the-gitlab-server-connection)
    - [Variables related to the MongoDB connection](#variables-related-to-the-mongodb-connection)
- [Installing GitLab data fetcher](#installing-gitlab-data-fetcher)
    - [Requirements](#requirements)
    - [Installation instructions](#installation-instructions)
- [API for the GitLab data fetcher](#api-for-the-gitlab-data-fetcher)
    - [`/all` endpoint](#all-endpoint)
    - [`/commits` endpoint](#commits-endpoint)
    - [`/files` endpoint](#files-endpoint)
    - [`/pipelines` endpoint](#pipelines-endpoint)
    - [`/info` endpoint](#info-endpoint)
    - [`/swagger` endpoint](#swagger-endpoint)
- [Notes about using GitLab data fetcher](#notes-about-using-gitlab-data-fetcher)

The GitLab data fetcher can be used to fetch raw data from a GitLab server.

Currently implemented data endpoints are:

- commits
    - [https://docs.gitlab.com/ee/api/commits.html](https://docs.gitlab.com/ee/api/commits.html)
- file data
    - [https://docs.gitlab.com/ee/api/repositories.html](https://docs.gitlab.com/ee/api/repositories.html) and [https://docs.gitlab.com/ee/api/repository_files.html](https://docs.gitlab.com/ee/api/repository_files.html)
- pipeline and job data
    - [https://docs.gitlab.com/ee/api/pipelines.html] and [https://docs.gitlab.com/ee/api/jobs.html](https://docs.gitlab.com/ee/api/jobs.html)

## Environment variables for the GitLab data fetcher

Before starting the GitLab data fetcher, the user must modify the environment variables. The file [`.env`](.env) contains a template for setting up the variables and some comments about what each variable is used for. All variables are also introduced in this section.

### Variables related to the deployment of the GitLab data fetcher

| Variable name      | Default value         | Description |
| ------------------ | --------------------- | ----------- |
| `APPLICATION_NAME` | visdom-fetcher-gitlab | The GitLab fetcher name, also used as the Docker container name |
| `HOST_NAME`        | localhost             | The host server name (only used for generating the Swagger definition) |
| `HOST_PORT`        | 8701                  | The host server port number for the GitLab fetcher API |

### Variables related to the GitLab server connection

| Variable name                | Default value | Description |
| ---------------------------- | ------------- | ----------- |
| `GITLAB_HOST`                |               | The GitLab host server address |
| `GITLAB_TOKEN`               |               | The GitLab API token for the host server|
| `GITLAB_INSECURE_CONNECTION` | false         | Is the GitLab host using an insecure certificate? (true/false) |

### Variables related to the MongoDB connection

the username and the password can be left empty if the used MongoDB instance does not use access control
| Variable name                | Default value  | Description |
| ---------------------------- | -------------- | -------------- |
| `MONGODB_HOST`               | visdom-mongodb | Mongo host name (either Docker container name or address to the Mongo host) |
| `MONGODB_PORT`               | 27017          | MongoDB port number |
| `MONGODB_NETWORK`            | visdom-network | Docker network name for the MongoDB (needed if MongoDB is only available from a private Docker network) |
| `MONGODB_METADATA_DATABASE`  | metadata       | The database that is used to store the metadata about the data fetcher (the same metadata database should be used for all the components in the data management system) |
| `MONGODB_DATA_DATABASE`      | gitlab         | The database that is used to store the fetched raw data from GitLab |
| `MONGODB_USERNAME`           |                | MongoDB username (must have read/write permission for the 2 databases) |
| `MONGODB_PASSWORD`           |                | MongoDB password (must have read/write permission for the 2 databases) |

## Installing GitLab data fetcher

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

The API for the GitLab data fetcher will be available at the port defined by `HOST_PORT` on the host machine.

The Swagger UI interface will be available at the address: `http://HOST_NAME:HOST_PORT/swagger`

## API for the GitLab data fetcher

| Endpoint     | Description                                        |
| ------------ | -------------------------------------------------- |
| `/all`       | start fetching data from all implemented endpoints |
| `/commits`   | start fetching commit data                         |
| `/files`     | start fetching repository file related data        |
| `/pipelines` | start fetching pipeline and job related data       |
| `/info`      | information about the GitLab data fetcher          |
| `/swagger`   | Swagger UI interface for the API                   |

### `/all` endpoint

Starts a fetching process for commit, file and pipeline data from a GitLab repository. All additional metadata and link data will be included, i.e. true used for all the boolean parameters for commits, files and pipelines endpoints. The actual data fetching is done in sequence: first commit data, then file data and finally pipeline data.

| Query parameter | Type     | Description |
| --------------- | -------- | ----------- |
| `projectName`   | required | the GitLab project name |
| `reference`     | optional | the reference (branch or tag) for the project, default: master |
| `startDate`     | optional | the earliest timestamp for the fetched data given in ISO 8601 format with timezone, default: no limit |
| `endDate`       | optional | the latest timestamp for the fetched data given in ISO 8601 format with timezone, default: no limit |

Successful query returns a response with a status code 202 which indicates that the data fetching process has been started. If there is a problem with the query, the returned status code will be either 400, 401, 404 or 500 depending on the problem. See the Swagger API definition for more details.

### `/commits` endpoint

Starts a fetching process for commit data from a GitLab repository.

| Query parameter         | Type     | Description |
| ----------------------- | -------- | ----------- |
| `projectName`           | required | the GitLab project name |
| `reference`             | optional | the reference (branch or tag) for the project, default: master |
| `startDate`             | optional | the earliest timestamp for the fetched commits given in ISO 8601 format with timezone, default: no limit |
| `endDate`               | optional | the latest timestamp for the fetched commits given in ISO 8601 format with timezone, default: no limit |
| `filePath`              | optional | the path for a file or folder to fetch commits for, default: fetch all commits |
| `includeStatistics`     | optional | whether statistics information is included or not (true/false, default: false) |
| `includeFileLinks`      | optional | whether file links information is included or not (true/false, default: false) |
| `includeReferenceLinks` | optional | whether reference links information is included or not (true/false, default: false) |

Successful query returns a response with a status code 202 which indicates that the data fetching process for the commit data has been started. If there is a problem with the query, the returned status code will be either 400, 401, 404 or 500 depending on the problem. See the Swagger API definition for more details.

The fetched commit data will be added to the collection `commits` in the MongoDB.

### `/files` endpoint

Starts a fetching process for file data from a GitLab repository.

| Query parameter      | Type     | Description |
| -------------------- | -------- | ----------- |
| `projectName`        | required | the GitLab project name |
| `reference`          | optional | the reference (branch or tag) for the project, default: master |
| `filePath`           | optional | the path inside repository to allow getting content of subdirectories, default fetch all files |
| `recursive`          | optional | whether to use recursive search or not (true/false, default: true) |
| `includeCommitLinks` | optional | whether commit links information is included or not (true/false, default: false) |

Successful query returns a response with a status code 202 which indicates that the data fetching process for the commit data has been started. If there is a problem with the query, the returned status code will be either 400, 401, 404 or 500 depending on the problem. See the Swagger API definition for more details.

The fetched file data will be added to the collection `files` in the MongoDB.

### `/pipelines` endpoint

Starts a fetching process for pipeline and job data from a GitLab repository.

| Query parameter  | Type     | Description |
| ---------------- | -------- | ----------- |
| `projectName`    | required | the GitLab project name |
| `reference`      | optional | the reference (branch or tag) for the project, default: master |
| `startDate`      | optional | the earliest timestamp for the fetched pipelines given in ISO 8601 format with timezone, default: no limit |
| `endDate`        | optional | the latest timestamp for the fetched pipelines given in ISO 8601 format with timezone, default: no limit |
| `includeJobs`    | optional | whether to fetch related job data or not (true/false, default: true) |
| `includeJobLogs` | optional | whether job logs are included or not (only applicable when includeJobs is true) (true/false, default: false) |

The fetched pipeline data will be added to the collection `pipelines` in the MongoDB.
If the includeJobs option is used, the fetched job data will be added to the collection `jobs`.
If the includeJobLogs option is used, the fetched job logs will be added to the collection `job_logs`.

### `/info` endpoint

The endpoint returns a status code 200 and JSON formatted object containing information about the GitLab data fetcher.

### `/swagger` endpoint

The GitLab data fetcher provides a Swagger UI interface to test the GitLab data fetcher API. It also contains some examples about possible responses in different problem cases.

## Notes about using GitLab data fetcher

- The response (status code: 202) from the API is given at the same time that the actual data fetching process is started. Depending on how much data the fetch results in, it might take a long while until the actual data fetching process is completed.
- No limits on how many data fetching can be start concurrently have been implemented.
- No access control for GitLab data fetcher has been implemented.
    - Any project that can be accessed with the access token given at startup will be available for data fetching without any tokens from the user.
- When the data fetching query results in new data, that data is stored as documents in the appropriate collection.
- When the data fetching query results in previously stored data, the stored document will be replaced by the new data fetch result.
    - This means that if the new query result did not include link data any possible link data in the previously stored document will be lost.
    - Documents stored in the database are identified by the combination of the following:
        - GitLab project name (attribute: `project_name`)
        - GitLab host server name (attribute: `host_name`)
        - Data specific identifying attribute (attribute: `path` for file data and `id` for other data)
- The `_metadata` attribute in the stored documents contains a timestamp for the latest update for that document.
- A new document will be added to the collection `metadata` after each completed data fetch query. This document will contain the number of added/updated documents.
    - Note that for the pipelines endpoint, the number of documents refer only to the number of pipeline related documents and does not include the affected job or job log related documents.
