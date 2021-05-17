# MongoDB for the Data Management System

## Install MongoDB

- Edit file [`.env`](.env) (port numbers, admin username and password, etc.). By default no authentication is used with MongoDB.
- Run command: `./start_mongo.sh`

## Uninstall MongoDB

- Run command: `./stop_mongo.sh`

## Remove the Mongo data

- Run command: `./remove_data.sh` (works only if MongoDB is not running)
