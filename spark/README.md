# Apache Spark for the Data Management System

[Apache Spark](https://spark.apache.org/) is a analytics engine for large-scale data processing. In the VISDOM data management system it is used to adapt the raw data stored in MongoDB to something the visualizations can use.

## Overall description

The Apache Spark system consists of a master component and worker components. Although, using these instructions the master and the worker components are all deployed in the same host server, the worker components can be deployed in different servers to utilize the processing power of a distributed server cluster.

The worker components register to the master at startup and when a new job is send to the master, it distributes the calculations to the available worker components.

## Installing Apache Spark

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

1. Edit the environmental variable file: [.env](.env) (the number of Spark workers, used port numbers, etc.)
    - the environment file contains explanations about the available variables
2. Run the command: `./start_spark.sh`
    - This will first create `docker-compose.yml` file with the number of worker components corresponding to the value defined by `SPARK_WORKERS` (by default: `1`) and then start all the containers defined in the created file.

The web UI for the Spark master will be available at the port defined by `SPARK_MASTER_UI_PORT` (by default: `8100`) on the host server. The web UI for the Spark workers will be available at the following ports (`8101`, `8102`, ...).

## Uninstalling Apache Spark

- Run the command: `./stop_spark.sh`
