// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.utils

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.WriteConfig
import java.time.Instant
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.bson.conversions.Bson
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.Document
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.QueryCache
import visdom.adapters.options.BaseQueryWithPageOptions
import visdom.adapters.options.SingleQueryOptions
import visdom.adapters.results.MultiResult
import visdom.adapters.results.Result
import visdom.adapters.results.ResultCounts
import visdom.adapters.results.SingleResult
import visdom.database.mongodb.MongoConnection
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.spark.ConfigUtils
import visdom.utils.SnakeCaseConstants


class GeneralQueryUtils(cacheDatabaseName: String, supportedDatabases: Seq[String]) {
    def getWriteConfig(sparkSession: SparkSession, collectionName: String): WriteConfig = {
        ConfigUtils.getWriteConfig(
            sparkSession,
            cacheDatabaseName,
            collectionName
        )
    }

    def isCacheUpdated(objectType: String): Boolean = {
        MongoConnection.getCacheUpdateTime(cacheDatabaseName, objectType) match {
            case Some(cacheUpdateTime: Instant) =>
                QueryCache.getLastDatabaseUpdateTime(supportedDatabases) match {
                    case Some(dataUpdateTime: Instant) => cacheUpdateTime.compareTo(dataUpdateTime) > 0
                    case None => true
                }
            case None => false
        }
    }

    def updateCache[ObjectType](
        sparkSession: SparkSession,
        objects: Dataset[ObjectType],
        objectType: String
    ): Unit = {
        MongoSpark.save(
            objects,
            getWriteConfig(sparkSession, objectType)
        )
    }

    def getCacheMetadataDocument(objectType: String): Document = {
        Document(
            Map(
                MongoConstants.AttributeType -> JsonUtils.toBsonValue(objectType),
                MongoConstants.AttributeTimestamp -> BsonDateTime(Instant.now().toEpochMilli())
            )
        )
    }

    def updateCacheMetadata(objectType: String): Unit = {
         MongoConnection.storeDocument(
            MongoConnection.getCollection(cacheDatabaseName, MongoConstants.CollectionMetadata),
            getCacheMetadataDocument(objectType),
            Array(MongoConstants.AttributeType)
         )
    }

    def storeObjects[ObjectType](
        sparkSession: SparkSession,
        objects: Dataset[ObjectType],
        objectType: String
    ): Unit = {
        updateCache(sparkSession, objects, objectType)
        updateCacheMetadata(objectType)
    }

    private def cleanCacheResult(
        result: BsonDocument,
        dataAttributes: Option[Seq[String]],
        extraAttributes: Seq[String]
    ): BsonDocument = {
        val initialResult: BsonDocument = result
            .removeAttribute(SnakeCaseConstants._Id)
            .removeAttribute(SnakeCaseConstants.CategoryIndex)
            .removeAttribute(SnakeCaseConstants.TypeIndex)
            .removeAttributes(extraAttributes)

        dataAttributes match {
            case Some(attributes: Seq[String]) => initialResult.getDocumentOption(SnakeCaseConstants.Data) match {
                case Some(dataDocument: BsonDocument) =>
                    initialResult.append(SnakeCaseConstants.Data, dataDocument.filterAttributes(attributes))
                case None => initialResult
            }
            case None => initialResult
        }
    }

    private def cleanCacheResults(
        results: Seq[BsonDocument],
        dataAttributes: Option[Seq[String]],
        extraAttributes: Seq[String],
        indexAttribute: String
    ): Seq[JsValue] = {
        results
            .sortBy(
                document => document.getIntOption(indexAttribute) match {
                    case Some(index: Int) => index
                    case None => 0
                }
            )
            .map(document => JsonUtils.toJsonValue(cleanCacheResult(document, dataAttributes, extraAttributes)))
    }

    private def toResult(resultData: Seq[JsValue], totalCount: Int, pageOptions: BaseQueryWithPageOptions): Result = {
        Result(
            counts = ResultCounts(
                count = resultData.size,
                totalCount = totalCount,
                page = pageOptions.page,
                pageSize = pageOptions.pageSize
            ),
            results = MultiResult(
                results = resultData
            )
        )
    }

    private def getCacheResults(
        objectTypes: Seq[String],
        pageOptions: BaseQueryWithPageOptions,
        dataAttributes: Option[Seq[String]],
        extraAttributes: Seq[String],
        indexAttribute: String
    ): Result = {
        val cacheCollections: Seq[MongoCollection[Document]] =
            objectTypes.map(objectType => MongoConnection.getCollection(cacheDatabaseName, objectType))

        val resultData: Seq[JsValue] = cleanCacheResults(
            cacheCollections.map(
                collection => MongoConnection.getDocuments(
                    collection,
                    List(
                        MongoConnection.getBetweenFilter(
                            indexAttribute,
                            JsonUtils.toBsonValue(AdapterUtils.getFirstIndex(pageOptions)),
                            JsonUtils.toBsonValue(AdapterUtils.getLastIndex(pageOptions))
                        )
                    )
                )
                    .map(document => document.toBsonDocument)
            ).flatten,
            dataAttributes,
            extraAttributes,
            indexAttribute
        )

        toResult(
            resultData,
            cacheCollections.map(collection => MongoConnection.getDocumentCount(collection).toInt).sum,
            pageOptions
        )
    }

    private def getCacheResults(
        objectTypes: Seq[String],
        pageOptions: BaseQueryWithPageOptions,
        dataAttributes: Option[Seq[String]],
        extraAttributes: Seq[String],
        indexAttribute: String,
        filter: Bson
    ): Result = {
        val cacheCollections: Seq[MongoCollection[Document]] =
            objectTypes.map(objectType => MongoConnection.getCollection(cacheDatabaseName, objectType))

        val resultDataFull: Seq[JsValue] = cleanCacheResults(
            cacheCollections.map(
                collection => MongoConnection
                    .getDocuments(collection, List(filter))
                    .map(document => document.toBsonDocument)
            ).flatten,
            dataAttributes,
            extraAttributes,
            indexAttribute
        )

        val resultData: Seq[JsValue] =
            resultDataFull
                .drop(AdapterUtils.getFirstIndex(pageOptions) - 1)
                .take(pageOptions.pageSize)

        toResult(resultData, resultDataFull.size, pageOptions)
    }

    def getCacheResults(
        objectType: String,
        pageOptions: BaseQueryWithPageOptions,
        dataAttributes: Option[Seq[String]],
        extraAttributes: Seq[String]
    ): Result = {
        getCacheResults(Seq(objectType), pageOptions, dataAttributes, extraAttributes, SnakeCaseConstants.TypeIndex)
    }

    def getCacheResults(
        objectTypes: Seq[String],
        pageOptions: BaseQueryWithPageOptions,
        dataAttributes: Option[Seq[String]],
        extraAttributes: Seq[String]
    ): Result = {
        getCacheResults(
            objectTypes,
            pageOptions,
            dataAttributes,
            extraAttributes,
            objectTypes.size match {
                case 1 => SnakeCaseConstants.TypeIndex
                case _ => SnakeCaseConstants.CategoryIndex
            }
        )
    }

    def getCacheResults(
        objectType: String,
        pageOptions: BaseQueryWithPageOptions,
        dataAttributes: Option[Seq[String]],
        extraAttributes: Seq[String],
        filter: Bson
    ): Result = {
        getCacheResults(
            Seq(objectType),
            pageOptions,
            dataAttributes,
            extraAttributes,
            SnakeCaseConstants.TypeIndex,
            filter
        )
    }

    def getCacheResults(
        objectTypes: Seq[String],
        pageOptions: BaseQueryWithPageOptions,
        dataAttributes: Option[Seq[String]],
        extraAttributes: Seq[String],
        filter: Bson
    ): Result = {
        getCacheResults(
            objectTypes,
            pageOptions,
            dataAttributes,
            extraAttributes,
            objectTypes.size match {
                case 1 => SnakeCaseConstants.TypeIndex
                case _ => SnakeCaseConstants.CategoryIndex
            },
            filter
        )
    }

    def getCacheResult(singleOptions: SingleQueryOptions): Option[SingleResult] = {
        MongoConnection.getDocuments(
            MongoConnection.getCollection(cacheDatabaseName, singleOptions.objectType),
            List(
                MongoConnection.getEqualFilter(SnakeCaseConstants.Id, JsonUtils.toBsonValue(singleOptions.uuid))
            )
        )
        .headOption
        .map(document => JsonUtils.toJsonValue(cleanCacheResult(document.toBsonDocument, None, Seq.empty)) match {
            case jsObject: JsObject => Some(SingleResult(jsObject))
            case _ => None
        })
        .flatten
    }
}
