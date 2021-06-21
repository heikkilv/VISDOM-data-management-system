package visdom.database.mongodb

import org.mongodb.scala.Document
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoClientSettings
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoCredential
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.ServerAddress
import org.mongodb.scala.SingleObservable
import org.mongodb.scala.bson.BsonNull
import org.mongodb.scala.bson.BsonObjectId
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.ReplaceOptions
import org.mongodb.scala.model.UpdateOptions
import org.mongodb.scala.result.InsertManyResult
import org.mongodb.scala.result.UpdateResult
import scala.collection.JavaConverters.seqAsJavaListConverter
import visdom.utils.WartRemoverConstants.WartsNonUnitStatements
import visdom.utils.EnvironmentVariables.EnvironmentMetadataDatabase
import visdom.utils.EnvironmentVariables.getEnvironmentVariable
import org.bson.BsonValue
import scala.concurrent.Await
import java.util.concurrent.TimeoutException
import org.mongodb.scala.FindObservable


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

    def getDocuments(
        collection: MongoCollection[Document],
        equalFilters: Map[String, BsonValue]
    ): Option[Document] = {
        val queryFilter: Bson = equalFilters.isEmpty match {
            case true => Document()
            case false => Filters.and(
                equalFilters.map(
                    equalFilter => Filters.equal(equalFilter._1, equalFilter._2)
                ).toSeq:_*
            )
        }

        val a = try {
            Await.result(
                collection.find(queryFilter).headOption(),
                MongoConstants.DefaultMaxQueryDelay
            )
        } catch  {
            case _: TimeoutException => None
        }
        a match {
            case Some(value) => {
                println("value:")
                println(value)
            }
            case None => println(None)
        }

        a
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
}
