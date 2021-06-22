#!/bin/bash

# Script for adding a user to MongoDB with read/write permissions to the appropriate databases

mongo $MONGODB_CONTAINER:$MONGODB_PORT/$MONGO_INITDB_DATABASE mongodb-init.js
