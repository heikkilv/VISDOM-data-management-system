// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapter.gitlab

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.ReadConfig
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.column
import org.apache.spark.sql.functions.udf
import spray.json.JsObject
import visdom.adapter.gitlab.results.CommitResult
import visdom.adapter.gitlab.utils.JsonUtils
import visdom.spark.Constants
import visdom.http.server.adapter.gitlab.CommitDataQueryOptions


object CommitQuery {
    implicit class EnrichedDataSet[T](dataset: Dataset[T]) {
        def applyProjectFilter(projectNameOption: Option[String]): Dataset[T] = {
            projectNameOption match {
                case Some(projectName: String) =>
                    dataset.filter(column(GitlabConstants.ColumnProjectName) === projectName)
                case None => dataset
            }
        }

        def applyUserFilter(userNameOption: Option[String]): Dataset[T] = {
            userNameOption match {
                case Some(userName: String) =>
                    dataset.filter(column(GitlabConstants.ColumnCommitterName) === userName)
                case None => dataset
            }
        }

        def applyDateFilter(dateStringOption: Option[String], isStartFilter: Boolean): Dataset[T] = {
            dateStringOption match {
                case Some(dateString: String) => isStartFilter match {
                    case true => dataset.filter(column(GitlabConstants.ColumnDate) >= dateString)
                    case false => dataset.filter(column(GitlabConstants.ColumnDate) <= dateString)
                }
                case None => dataset
            }
        }

        def applyFilters(queryOptions: CommitDataQueryOptions): Dataset[T] = {
            dataset
                .applyProjectFilter(queryOptions.projectName)
                .applyUserFilter(queryOptions.userName)
                .applyDateFilter(queryOptions.startDate, true)
                .applyDateFilter(queryOptions.endDate, false)
        }
    }

    def utcStringToDate: String => String = {
        datetimeString => datetimeString.substring(0, GitlabConstants.DateStringLength)
    }
    val dateStringUDF: UserDefinedFunction = udf(utcStringToDate)

    def getDataFrame(
        sparkSession: SparkSession,
        queryOptions: CommitDataQueryOptions
    ): Dataset[CommitResult] = {
        val commitReadConfig: ReadConfig = ReadConfig(
            databaseName = Constants.DefaultDatabaseName,
            collectionName = GitlabConstants.CollectionCommits,
            connectionString = sparkSession.sparkContext.getConf.getOption(
                Constants.MongoInputUriSetting
            )
        )

        MongoSpark.load[schemas.CommitSchema](sparkSession, commitReadConfig)
            .select(
                column(GitlabConstants.ColumnProjectName),
                column(GitlabConstants.ColumnCommitterName),
                dateStringUDF(
                    column(GitlabConstants.ColumnCommittedDate)
                ).as(GitlabConstants.ColumnDate)
            )
            .applyFilters(queryOptions)
            .groupBy(
                GitlabConstants.ColumnProjectName,
                GitlabConstants.ColumnCommitterName,
                GitlabConstants.ColumnDate
            )
            .count()
            .as(Encoders.product[CommitResult])
    }

    def getResult(sparkSession: SparkSession, queryOptions: CommitDataQueryOptions): JsObject = {
        val results: Array[CommitResult] = getDataFrame(sparkSession, queryOptions).collect()
        JsonUtils.toJsObject(results.map(result => result.toJsonTuple()))
    }
}
