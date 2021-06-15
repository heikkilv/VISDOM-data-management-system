package visdom.adapter.gitlab

import java.time.Instant
import org.mongodb.scala.Document
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoClientSettings
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoCredential
import org.mongodb.scala.ServerAddress
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.bson.BsonObjectId
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.ReplaceOptions
import org.mongodb.scala.result.UpdateResult
import scala.collection.JavaConverters.seqAsJavaListConverter
import visdom.spark.Constants


object Metadata {
    val AttributeDefaultId: String = "id"
    val AttributeAdapterType: String = "adapter_type"
    val AttributeApplicationName: String = "application_name"
    val AttributeComponentType: String = "component_type"
    val AttributeDatabase: String = "database"
    val AttributeStartTime: String = "start_time"
    val AttributeTimestamp: String = "timestamp"
    val AttributeVersion: String = "version"

    val metadataCollection: MongoCollection[Document] =
        MongoClient(Constants.DefaultMongoUri)
            .getDatabase(Constants.MetadataDatabaseName)
            .getCollection(Constants.DefaultMongoCollection)

    def storeDocument(
        collection: MongoCollection[Document],
        document: Document,
        identifierAttributes: Array[String]
    ): Unit = {
        val documentFilter: Bson = identifierAttributes.isEmpty match {
            case true => Filters.equal(AttributeDefaultId, BsonObjectId())
            case false => Filters.and(
                identifierAttributes.map(
                    identifierName => Filters.equal(
                        identifierName,
                        document.getOrElse(identifierName, new BsonNull)
                    )
                ):_*
            )
        }

        collection.replaceOne(
            documentFilter,
            document,
            ReplaceOptions().upsert(true)
        ).subscribe(
            doOnNext = (result: UpdateResult) =>
                result.getMatchedCount() match {
                    case 0 =>  println(s"document ${result.getUpsertedId()} inserted")
                    case _ =>  println(s"${result.getModifiedCount()} document updated")
                },
            doOnError = (error: Throwable) =>
                println(s"Database error: ${error.toString()}")
        )
    }

    def storeMetadata(): Unit = {
        val metadataDocument: Document = Document(
            BsonDocument(
                AttributeApplicationName -> Adapter.AdapterName,
                AttributeComponentType -> GitlabConstants.ComponentType,
                AttributeAdapterType -> GitlabConstants.AdapterType,
                AttributeVersion -> GitlabConstants.AdapterVersion,
                AttributeDatabase -> Constants.DefaultDatabaseName,
                AttributeStartTime -> Adapter.StartTime
            )
            .append(AttributeTimestamp, BsonDateTime(Instant.now().toEpochMilli()))
        )
        storeDocument(
            metadataCollection,
            metadataDocument,
            Array(
                AttributeApplicationName,
                AttributeComponentType,
                AttributeAdapterType,
                AttributeVersion
            )
        )
    }
}
