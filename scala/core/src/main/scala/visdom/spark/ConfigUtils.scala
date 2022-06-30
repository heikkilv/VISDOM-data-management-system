// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.spark

import com.mongodb.spark.config.ReadConfig
import com.mongodb.spark.config.WriteConfig
import org.apache.spark.sql.SparkSession


object ConfigUtils {
    def getReadConfig(sparkSession: SparkSession, databaseName: String, collectionName: String): ReadConfig = {
        ReadConfig(
            databaseName = databaseName,
            collectionName = collectionName,
            connectionString = sparkSession.sparkContext.getConf.getOption(Constants.MongoInputUriSetting)
        )
    }

    def getWriteConfig(sparkSession: SparkSession, databaseName: String, collectionName: String): WriteConfig = {
        WriteConfig(
            databaseName = databaseName,
            collectionName = collectionName,
            connectionString = sparkSession.sparkContext.getConf.getOption(Constants.MongoOutputUriSetting),
            replaceDocument = true
        )
    }
}
