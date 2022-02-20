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
import visdom.adapters.general.AdapterValues
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


object GeneralQueryUtils {
    def getWriteConfig(sparkSession: SparkSession, collectionName: String): WriteConfig = {
        ConfigUtils.getWriteConfig(
            sparkSession,
            AdapterValues.cacheDatabaseName,
            collectionName
        )
    }

    def isCacheUpdated(objectType: String): Boolean = {
        MongoConnection.getCacheUpdateTime(AdapterValues.cacheDatabaseName, objectType) match {
            case Some(cacheUpdateTime: Instant) =>
                QueryCache.getLastDatabaseUpdateTime(
                    Seq(AdapterValues.gitlabDatabaseName, AdapterValues.aPlusDatabaseName)
                ) match {
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
            MongoConnection.getCollection(AdapterValues.cacheDatabaseName, MongoConstants.CollectionMetadata),
            getCacheMetadataDocument(objectType),
            Array(MongoConstants.AttributeType)
         )
    }

    def storeObjects[ObjectType](
        sparkSession: SparkSession,
        objects: Dataset[ObjectType],
        objectType: String
    ): Unit = {
        GeneralQueryUtils.updateCache(sparkSession, objects, objectType)
        GeneralQueryUtils.updateCacheMetadata(objectType)
    }

    private def cleanCacheResult(result: BsonDocument): BsonDocument = {
        result
            .removeAttribute(SnakeCaseConstants._Id)
            .removeAttribute(SnakeCaseConstants.CategoryIndex)
            .removeAttribute(SnakeCaseConstants.TypeIndex)
    }

    private def cleanCacheResults(results: Seq[BsonDocument], indexAttribute: String): Seq[JsValue] = {
        results
            .sortBy(
                document => document.getIntOption(indexAttribute) match {
                    case Some(categoryId: Int) => categoryId
                    case None => 0
                }
            )
            .map(document => JsonUtils.toJsonValue(cleanCacheResult(document)))
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
        indexAttribute: String
    ): Result = {
        val cacheCollections: Seq[MongoCollection[Document]] =
            objectTypes.map(objectType => MongoConnection.getCollection(AdapterValues.cacheDatabaseName, objectType))
        val totalCount: Int =
            cacheCollections.map(collection => MongoConnection.getDocumentCount(collection).toInt).sum

        val resultData: Seq[JsValue] = cleanCacheResults(
            cacheCollections.map(
                collection => MongoConnection.getDocuments(
                    collection,
                    List(
                        MongoConnection.getBetweenFilter(
                            indexAttribute,
                            JsonUtils.toBsonValue(AdapterUtils.getFirstIndex(pageOptions, totalCount)),
                            JsonUtils.toBsonValue(AdapterUtils.getLastIndex(pageOptions, totalCount))
                        )
                    )
                )
                    .map(document => document.toBsonDocument)
            ).flatten,
            indexAttribute
        )

        toResult(resultData, totalCount, pageOptions)
    }

    private def getCacheResults(
        objectTypes: Seq[String],
        pageOptions: BaseQueryWithPageOptions,
        indexAttribute: String,
        filter: Bson
    ): Result = {
        val cacheCollections: Seq[MongoCollection[Document]] =
            objectTypes.map(objectType => MongoConnection.getCollection(AdapterValues.cacheDatabaseName, objectType))

        val resultDataFull: Seq[JsValue] = cleanCacheResults(
            cacheCollections.map(
                collection => MongoConnection
                    .getDocuments(collection, List(filter))
                    .map(document => document.toBsonDocument)
            ).flatten,
            indexAttribute
        )

        val resultData: Seq[JsValue] =
            resultDataFull
                .drop(AdapterUtils.getFirstIndex(pageOptions) - 1)
                .take(pageOptions.pageSize)

        toResult(resultData, resultDataFull.size, pageOptions)
    }

    def getCacheResults(objectType: String, pageOptions: BaseQueryWithPageOptions): Result = {
        getCacheResults(Seq(objectType), pageOptions, SnakeCaseConstants.TypeIndex)
    }

    def getCacheResults(objectTypes: Seq[String], pageOptions: BaseQueryWithPageOptions): Result = {
        getCacheResults(objectTypes, pageOptions, SnakeCaseConstants.CategoryIndex)
    }

    def getCacheResults(objectType: String, pageOptions: BaseQueryWithPageOptions, filter: Bson): Result = {
        getCacheResults(Seq(objectType), pageOptions, SnakeCaseConstants.TypeIndex, filter)
    }

    def getCacheResults(objectTypes: Seq[String], pageOptions: BaseQueryWithPageOptions, filter: Bson): Result = {
        getCacheResults(objectTypes, pageOptions, SnakeCaseConstants.CategoryIndex, filter)
    }

    def getCacheResult(singleOptions: SingleQueryOptions): Option[SingleResult] = {
        MongoConnection.getDocuments(
            MongoConnection.getCollection(AdapterValues.cacheDatabaseName, singleOptions.objectType),
            List(
                MongoConnection.getEqualFilter(SnakeCaseConstants.Id, JsonUtils.toBsonValue(singleOptions.uuid))
            )
        )
        .headOption
        .map(document => JsonUtils.toJsonValue(cleanCacheResult(document.toBsonDocument)) match {
            case jsObject: JsObject => Some(SingleResult(jsObject))
            case _ => None
        })
        .flatten
    }
}
