package visdom.spark

import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.SparkSession
import visdom.utils.CommonConstants


object Session {
    // Suppress the log messages
    Logger.getLogger(CommonConstants.Org).setLevel(Level.OFF)

    def getSparkSession(): SparkSession = {
        SparkSession
            .builder
            .master(Constants.SparkMaster)
            .appName(Constants.ApplicationName)
            .config(Constants.MongoInputUriSetting, Constants.DefaultMongoUri)
            .config(Constants.MongoOutputUriSetting, Constants.DefaultMongoUri)
            .config(Constants.SparkCoresMax, Constants.SparkCoresMaxDefault)
            .config(Constants.SparkSchedulerMode, Constants.SparkSchedulerModeDefault)
            .config(Constants.SparkDynamicAllocationEnabled, Constants.SparkDynamicAllocationEnabledDefault)
            .config(Constants.SparkDynamicAllocationShuffleTrackingEnabled, Constants.SparkDynamicAllocationShuffleTrackingEnabledDefault)
            .config(Constants.SparkDynamicAllocationExecutorIdleTimeout, Constants.SparkDynamicAllocationExecutorIdleTimeoutDefault)
            .config(Constants.SparkDynamicAllocationExecutorAllocationRatio, Constants.SparkDynamicAllocationExecutorAllocationRatioDefault)
            .config(Constants.SparkDynamicAllocationInitialExecutors, Constants.SparkDynamicAllocationInitialExecutorsDefault)
            .config(Constants.SparkDynamicAllocationMinExecutors, Constants.SparkDynamicAllocationMinExecutorsDefault)
            .config(Constants.SparkDynamicAllocationMaxExecutors, Constants.SparkDynamicAllocationMaxExecutorsDefault)
            .getOrCreate()
    }
}
