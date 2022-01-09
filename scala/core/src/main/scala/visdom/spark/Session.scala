package visdom.spark

import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.SparkSession
import visdom.spark.Constants._
import visdom.utils.CommonConstants


object Session {
    // Suppress the normal log messages
    Logger.getLogger(CommonConstants.Org).setLevel(Level.ERROR)

    def getSparkSession(): SparkSession = {
        SparkSession
            .builder
            .master(SparkMaster)
            .appName(ApplicationName)
            .config(MongoInputUriSetting, DefaultMongoUri)
            .config(MongoOutputUriSetting, DefaultMongoUri)
            .config(SparkSchedulerMode, SparkSchedulerModeDefault)
            .config(SparkDynamicAllocationEnabled, SparkDynamicAllocationEnabledDefault)
            .config(SparkDynamicAllocationShuffleTrackingEnabled, SparkDynamicAllocationShuffleTrackingEnabledDefault)
            .config(SparkDynamicAllocationExecutorIdleTimeout, SparkDynamicAllocationExecutorIdleTimeoutDefault)
            .config(SparkDynamicAllocationExecutorAllocationRatio, SparkDynamicAllocationExecutorAllocationRatioDefault)
            .config(SparkDynamicAllocationInitialExecutors, SparkDynamicAllocationInitialExecutorsDefault)
            .config(SparkDynamicAllocationMinExecutors, SparkDynamicAllocationMinExecutorsDefault)
            .config(SparkDynamicAllocationMaxExecutors, SparkDynamicAllocationMaxExecutorsDefault)
            .config(SparkSqlDatetimeJava8APIEnabled, SparkSqlDatetimeJava8APIEnabledDefault)
            .getOrCreate()
    }
}
