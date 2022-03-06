package visdom.spark

import visdom.utils.CommonConstants


object Constants {
    // The names for the environment variables
    val EnvironmentApplicationName: String = "APPLICATION_NAME"

    val EnvironmentSparkMaster: String = "SPARK_MASTER_NAME"
    val EnvironmentSparkPort: String = "SPARK_MASTER_PORT"

    val EnvironmentMongoHost: String = "MONGODB_HOST"
    val EnvironmentMongoPort: String = "MONGODB_PORT"
    val EnvironmentMongoUsername: String = "MONGODB_USERNAME"
    val EnvironmentMongoPassword: String = "MONGODB_PASSWORD"
    val EnvironmentMongoDatabase: String = "MONGODB_DATABASE"
    val EnvironmentMetadataDatabase: String = "MONGODB_METADATA_DATABASE"

    // The default values for the environment variables
    val DefaultApplicationName: String = "Application"

    val DefaultSparkMaster: String = "spark-master"
    val DefaultSparkPort: String = "7707"

    val DefaultMongoHost: String = "mongodb"
    val DefaultMongoPort: String = "27017"
    val DefaultMongoUsername: String = CommonConstants.EmptyString
    val DefaultMongoPassword: String = CommonConstants.EmptyString
    val DefaultMongoDatabase: String = "metadata"
    val DefaultMongoCollection: String = "metadata"
    val DefaultMetadataDatabase: String = "metadata"

    val MongoInputUriSetting: String = "spark.mongodb.input.uri"
    val MongoOutputUriSetting: String = "spark.mongodb.output.uri"

    val UriConnectionCharacter: String = CommonConstants.DoubleDot
    val SparkConnectionStringBase: String = "spark://"
    val MongoConnectionStringBase: String = "mongodb://"

    val SparkCoresMax: String = "spark.cores.max"
    val SparkSchedulerMode: String = "spark.scheduler.mode"
    val SparkDynamicAllocationEnabled: String = "spark.dynamicAllocation.enabled"
    val SparkDynamicAllocationShuffleTrackingEnabled: String = "spark.dynamicAllocation.shuffleTracking.enabled"
    val SparkDynamicAllocationExecutorIdleTimeout: String = "spark.dynamicAllocation.executorIdleTimeout"
    val SparkDynamicAllocationExecutorAllocationRatio: String = "spark.dynamicAllocation.executorAllocationRatio"
    val SparkDynamicAllocationInitialExecutors: String = "spark.dynamicAllocation.initialExecutors"
    val SparkDynamicAllocationMinExecutors: String = "spark.dynamicAllocation.minExecutors"
    val SparkDynamicAllocationMaxExecutors: String = "spark.dynamicAllocation.maxExecutors"
    val SparkSqlDatetimeJava8APIEnabled: String = "spark.sql.datetime.java8API.enabled"
    val SparkSqlShufflePartitions: String = "spark.sql.shuffle.partitions"

    val SparkCoresMaxDefault: Int = 6
    val SparkSchedulerModeDefault: String = "FAIR"
    val SparkDynamicAllocationEnabledDefault: Boolean = true
    val SparkDynamicAllocationShuffleTrackingEnabledDefault: Boolean = true
    val SparkDynamicAllocationExecutorIdleTimeoutDefault: String = "10s"
    val SparkDynamicAllocationExecutorAllocationRatioDefault: Double = 0.5
    val SparkDynamicAllocationInitialExecutorsDefault: Int = 1
    val SparkDynamicAllocationMinExecutorsDefault: Int = 1
    val SparkDynamicAllocationMaxExecutorsDefault: Int = 6
    val SparkSqlDatetimeJava8APIEnabledDefault: Boolean = true
    val SparkSqlShufflePartitionsDefault: Int = 5

    val ApplicationName: String = sys.env.getOrElse(EnvironmentApplicationName, DefaultApplicationName)

    val DefaultDatabaseName: String = sys.env.getOrElse(EnvironmentMongoDatabase, DefaultMongoDatabase)
    val MetadataDatabaseName: String = sys.env.getOrElse(EnvironmentMetadataDatabase, DefaultMetadataDatabase)

    val SparkMaster: String = Seq(
        SparkConnectionStringBase,
        Seq(
            sys.env.getOrElse(EnvironmentSparkMaster, DefaultSparkMaster),
            sys.env.getOrElse(EnvironmentSparkPort, DefaultSparkPort)
        ).mkString(UriConnectionCharacter)
    ).mkString

    val DefaultMongoUri: String = Seq(
        MongoConnectionStringBase,
        {
            val username: String = sys.env.getOrElse(EnvironmentMongoUsername, DefaultMongoUsername)
            username.isEmpty() match {
                case true => CommonConstants.EmptyString
                case false => Seq(
                    username,
                    sys.env.getOrElse(EnvironmentMongoPassword, DefaultMongoPassword)
                ).mkString(UriConnectionCharacter) + CommonConstants.AtSign
            }
        },
        Seq(
            sys.env.getOrElse(EnvironmentMongoHost, DefaultMongoHost),
            sys.env.getOrElse(EnvironmentMongoPort, DefaultMongoPort)
        ).mkString(UriConnectionCharacter),
        CommonConstants.Slash,
        DefaultMetadataDatabase,
        CommonConstants.Dot,
        DefaultMongoCollection
    ).mkString
}
