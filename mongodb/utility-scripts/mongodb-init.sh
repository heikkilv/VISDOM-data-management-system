#!/bin/bash
# Copyright 2022 Tampere University
# This software was developed as a part of the VISDOM project: https://iteavisdom.org/
# This source code is licensed under the MIT license. See LICENSE in the repository root directory.
# Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

# Script for adding a user to MongoDB with read/write permissions to the appropriate databases

mongo $MONGODB_CONTAINER:$MONGODB_PORT/$MONGO_INITDB_DATABASE mongodb-init.js
