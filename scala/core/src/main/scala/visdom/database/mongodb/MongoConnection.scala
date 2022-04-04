package visdom.database.mongodb

import java.time.Instant
import java.util.concurrent.TimeoutException
import org.bson.BsonValue
import com.mongodb.bulk.BulkWriteResult
import org.mongodb.scala.Document
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoClientSettings
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoCredential
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.ServerAddress
import org.mongodb.scala.SingleObservable
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonObjectId
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.model.Indexes
import org.mongodb.scala.model.ReplaceOptions
import org.mongodb.scala.model.Sorts
import org.mongodb.scala.model.UpdateOneModel
import org.mongodb.scala.model.UpdateOptions
import org.mongodb.scala.model.Updates
import org.mongodb.scala.result.InsertManyResult
import org.mongodb.scala.result.UpdateResult
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.concurrent.Await
import visdom.utils.EnvironmentVariables.EnvironmentMetadataDatabase
import visdom.utils.EnvironmentVariables.getEnvironmentVariable
import visdom.utils.WartRemoverConstants.WartsNonUnitStatements


object MongoConnection {
    private val environmentVariables: Map[String, String] = sys.env

    val applicationName: String = environmentVariables.getOrElse(
        MongoConstants.ApplicationName,
        MongoConstants.DefaultApplicationName
    )

    val metadataDatabaseName: String = getEnvironmentVariable(EnvironmentMetadataDatabase)

    private val mongoCredentials: MongoCredential = MongoCredential.createCredential(
        userName = environmentVariables.getOrElse(
            MongoConstants.MongoUserName,
            MongoConstants.DefaultMongoUserName
        ),
        database = metadataDatabaseName,
        password = environmentVariables.getOrElse[String](
            MongoConstants.MongoPassword,
            MongoConstants.DefaultMongoPassword
        ).toCharArray()
    )

    private val mongoServerAddress: ServerAddress = new ServerAddress(
        environmentVariables.getOrElse(
            MongoConstants.MongoHost,
            MongoConstants.DefaultMongoHost
        ),
        environmentVariables.get(MongoConstants.MongoPort) match {
            case Some(portString: String) => {
                try {
                    portString.toInt
                }
                catch {
                    case e: java.lang.NumberFormatException => MongoConstants.DefaultMongoPort
                }
            }
            case None => MongoConstants.DefaultMongoPort
        }
    )

    @SuppressWarnings(Array(WartsNonUnitStatements))
    val mongoClient: MongoClient = MongoClient({
        val clientSettingsBuilder: MongoClientSettings.Builder = MongoClientSettings.builder()
            .applicationName(applicationName)
            .applyToClusterSettings(
                (builder: ClusterSettings.Builder) => builder.hosts(
                    List(mongoServerAddress).asJava
                )
            )
        (mongoCredentials.getUserName().isEmpty match {
            case true => clientSettingsBuilder
            case false => clientSettingsBuilder.credential(mongoCredentials)
        }).build()
    })

    def getMainMetadataCollection(): MongoCollection[Document] = {
        mongoClient
            .getDatabase(metadataDatabaseName)
            .getCollection(MongoConstants.CollectionMetadata)
    }

    def getCollection(databaseName: String, collectionName: String): MongoCollection[Document] = {
        mongoClient
            .getDatabase(databaseName)
            .getCollection(collectionName)
    }

    def getLastUpdateTime(databaseName: String): Option[Instant] = {
        (
            try {
                Await.result(
                    mongoClient
                        .getDatabase(databaseName)
                        .getCollection(MongoConstants.CollectionMetadata)
                        .find()
                        .sort(Sorts.descending(MongoConstants.AttributeDefaultId))
                        .limit(1)
                        .headOption(),
                    MongoConstants.DefaultMaxQueryDelay
                )
            } catch {
                case _: TimeoutException => None
            }
        ) match {
            case Some(document: Document) => {
                document.get(MongoConstants.AttributeTimestamp) match {
                    case Some(timestamp: BsonDateTime) => Some(Instant.ofEpochMilli(timestamp.getValue()))
                    case _ => None
                }
            }
            case None => None
        }
    }

    def getCacheUpdateTime(databaseName: String, objectType: String): Option[Instant] = {
        (
            try {
                Await.result(
                    mongoClient
                        .getDatabase(databaseName)
                        .getCollection(MongoConstants.CollectionMetadata)
                        .find(Filters.equal(MongoConstants.AttributeType, objectType))
                        .limit(1)
                        .headOption(),
                    MongoConstants.DefaultMaxQueryDelay
                )
            } catch {
                case _: TimeoutException => None
            }
        ) match {
            case Some(document: Document) => {
                document.get(MongoConstants.AttributeTimestamp) match {
                    case Some(timestamp: BsonDateTime) => Some(Instant.ofEpochMilli(timestamp.getValue()))
                    case _ => None
                }
            }
            case None => None
        }
    }

    def getEqualFilter(attributeName: String, attributeValue: BsonValue): Bson = {
        Filters.equal(attributeName, attributeValue)
    }

    def getLessThanFilter(attributeName: String, attributeValue: BsonValue): Bson = {
        Filters.lt(attributeName, attributeValue)
    }

    def getLessThanEqualFilter(attributeName: String, attributeValue: BsonValue): Bson = {
        Filters.lte(attributeName, attributeValue)
    }

    def getGreaterThanFilter(attributeName: String, attributeValue: BsonValue): Bson = {
        Filters.gt(attributeName, attributeValue)
    }

    def getGreaterThanEqualFilter(attributeName: String, attributeValue: BsonValue): Bson = {
        Filters.gte(attributeName, attributeValue)
    }

    def getBetweenFilter(attribute: String, lowLimit: BsonValue, highLimit: BsonValue): Bson = {
        Filters.and(
            getGreaterThanEqualFilter(attribute, lowLimit),
            getLessThanEqualFilter(attribute, highLimit)
        )
    }

    def getDocumentCount(collection: MongoCollection[Document]): Long = {
        (
            try {
                Await.result(
                    collection.countDocuments().headOption(),
                    MongoConstants.DefaultMaxQueryDelay
                )
            } catch {
                case _: TimeoutException => None
            }
        ) match {
            case Some(count: Long) => count
            case None => 0
        }
    }

    def getDocuments(
        collection: MongoCollection[Document],
        filters: List[Bson]
    ): List[Document] = {
        val queryFilter: Bson = filters.isEmpty match {
            case true => Document()
            case false => Filters.and(filters:_*)
        }

        val resultOption: Option[Seq[Document]] = try {
            Await.result(
                collection.find(queryFilter).collect().headOption(),
                MongoConstants.DefaultMaxQueryDelay
            )
        } catch {
            case _: TimeoutException => None
        }

        resultOption match {
            case Some(results: Seq[Document]) => results.toList
            case None => List()
        }
    }

    def storeDocument(
        collection: MongoCollection[Document],
        document: Document,
        identifierAttributes: Array[String]
    ): Unit = {
        val documentFilter: Bson = identifierAttributes.isEmpty match {
            case true => Filters.equal(
                MongoConstants.AttributeDefaultId,
                BsonObjectId()
            )
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
                    case 0 => // println(s"document ${result.getUpsertedId()} inserted")
                    case _ => // println(s"${result.getModifiedCount()} document updated")
                },
            doOnError = (error: Throwable) =>
                println(s"Database error: ${error.toString()}")
        )
    }

    def updateManyDocuments(
        collection: MongoCollection[Document],
        updates: Seq[(Bson, Document)]
    ): Unit = {
        collection.bulkWrite(
            updates.map({
                case (filter, update) => UpdateOneModel(
                    filter = filter,
                    update = Updates.combine(update.map({case (key, value) => Updates.set(key, value)}).toSeq:_*)
                )
            })
        ).subscribe(
            doOnNext = (result: BulkWriteResult) =>
                println(
                    s"Updated ${result.getModifiedCount()}/${updates.size} " +
                    s"documents in collection ${collection.namespace.getCollectionName()}"
                ),
            doOnError = (error: Throwable) =>
                println(s"Database error: ${error.toString()}")
        )
    }

    def updateDocument(
        collection: MongoCollection[Document],
        update: Document,
        identifyFilter: Bson
    ): Unit = {
        collection.updateOne(
            identifyFilter,
            Updates.combine(update.map({case (key, value) => Updates.set(key, value)}).toSeq:_*)
        ).subscribe(
            doOnNext = (result: UpdateResult) =>
                result.getMatchedCount() match {
                    case 1 =>  // successful update
                    case _ =>  // no updates done
                },
            doOnError = (error: Throwable) =>
                println(s"Database error: ${error.toString()}")
        )
    }

    def printCreateIndexesMessage(collectionName: String, indexName: String): Unit = {
        // println(s"Created index ${indexName} for collection ${collectionName}")
    }

    def createIndexes(
        collection: MongoCollection[Document],
        indexAttributes: Seq[String]
    ): Unit = {
        collection.createIndexes(
            indexAttributes.map(attribute => IndexModel(Indexes.ascending(attribute)))
        ).subscribe(
            doOnNext = (result: String) => printCreateIndexesMessage(collection.namespace.getCollectionName(), result),
            doOnError = (error: Throwable) =>
                println(s"Database error: ${error.toString()}")
        )
    }
}
