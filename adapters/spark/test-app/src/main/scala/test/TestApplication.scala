package test

import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.functions.sum


object TestApplication {
    def main(args: Array[String]) {
        // Suppress the log messages
        Logger.getLogger("org").setLevel(Level.OFF)

        val EnvironmentApplicationName: String = "APPLICATION_NAME"
        val EnvironmentSparkMaster: String = "SPARK_MASTER_NAME"
        val EnvironmentSparkPort: String = "SPARK_MASTER_PORT"
        val DefaultApplicationName: String = "Test"
        val DefaultSparkMaster: String = "spark-master"
        val DefaultSparkPort: String = "7707"

        val applicationName: String = sys.env.getOrElse(EnvironmentApplicationName, DefaultApplicationName)
        val sparkMaster: String = "spark://" + Seq(
            sys.env.getOrElse(EnvironmentSparkMaster, DefaultSparkMaster),
            sys.env.getOrElse(EnvironmentSparkPort, DefaultSparkPort)
        ).mkString(":")

        val NameColumn: String = "name"
        val ValueColumn: String = "value"
        val SumColumn: String = "value_sum"

        val spark: SparkSession = SparkSession
            .builder
            .master(sparkMaster)
            .appName(applicationName)
            .getOrCreate()

        val dataSequence: Seq[Row] = Seq(
            Row("ddd", 0.11), Row("bbb", 1.17), Row("aaa", 2.13),
            Row("bbb", 0.52), Row("ccc", 1.56), Row("bbb", 3.54),
            Row("ccc", 0.83), Row("aaa", 0.39), Row("aaa", 4.85),
            Row("ddd", 0.74), Row("ddd", 5.70), Row("bbb", 5.76),
            Row("ccc", 2.25), Row("bbb", 1.21), Row("bbb", 6.27),
            Row("ccc", 2.46), Row("ddd", 3.42), Row("ccc", 7.48)
        )
        val dataRDD: RDD[Row] = spark.sparkContext.parallelize(dataSequence)

        val manualSchema: StructType = new StructType(Array(
            new StructField(NameColumn, StringType, false),
            new StructField(ValueColumn, DoubleType, false)))
        val dataDF: DataFrame = spark.createDataFrame(dataRDD, manualSchema)

        println("The entire data collection:")
        dataDF.show()

        val roundTo2 = (value: Double) => (value * 100).round / 100.toDouble

        val valueSumDF: DataFrame = dataDF
            .groupBy(NameColumn)
            .agg(sum(ValueColumn) as SumColumn)
            .orderBy(col(SumColumn).desc)
        val valueSums: Array[(String, Double)] = valueSumDF
            .collect()
            .map(row => (
                row.get(0).asInstanceOf[String],
                roundTo2(row.get(1).asInstanceOf[Double])
            ))

        println("The summed values:")
        valueSums.foreach({
            case (name, value) => println(s"name: ${name}:  sum: ${value}")
        })

        spark.stop()
    }
}
