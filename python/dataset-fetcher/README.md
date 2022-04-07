# Dataset fetcher

This tool can be used to to modify the project data collected from the dataset [https://github.com/clowee/The-Technical-Debt-Dataset/releases/tag/2.0.1](https://github.com/clowee/The-Technical-Debt-Dataset/releases/tag/2.0.1) to MongoDB so that it is compatible with the data adapters in the VISDOM data management system. It works as dataset fetcher that provides the raw data for the [Dataset adapter](../../documentation/adapters/dataset/README.md).

## Requirements

- Docker
- Docker Compose
- Running MongoDB instance
    - with write permissions to the target database

## Running instructions

1. Fetch the dataset given in SQLite format

    ```bash
    wget https://github.com/clowee/The-Technical-Debt-Dataset/releases/download/2.0.1/td_V2.db
    ```

2. Edit the MongoDB connection related details

    ```bash
    cp .env.template .env
    ```

    and then edit the values for the variables in file `.env`

3. Run the project data converter

    ```bash
    docker-compose up --build
    ```

## Cleanup instructions

1. Remove all created Docker containers

    ```bash
    docker-compose down --remove-orphans
    ```

2. Remove created Docker image

    ```bash
    docker rmi visdom/sqlite-mongodb-converter:0.1
    ```

3. Remove the SQLite database

    ```bash
    rm td_V2.db
    ```
