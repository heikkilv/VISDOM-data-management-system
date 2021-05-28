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


object MongoConnection {
    private val environmentVariables: Map[String, String] = sys.env

    private val applicationName: String = environmentVariables.getOrElse(
        MongoConstants.ApplicationName,
        MongoConstants.DefaultApplicationName
    )

    private val mongoCredentials: MongoCredential = MongoCredential.createCredential(
        userName = environmentVariables.getOrElse(
            MongoConstants.MongoUserName,
            MongoConstants.DefaultMongoUserName
        ),
        database = environmentVariables.getOrElse(
            MongoConstants.MongoMetadataDatabase,
            MongoConstants.DefaultMongoMetadataDatabase
        ),
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

    @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
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
                    case 0 => println(s"document ${result.getUpsertedId()} inserted")
                    case _ => println(s"${result.getModifiedCount()} document updated")
                },
            doOnError = (error: Throwable) =>
                println(s"Database error: ${error.toString()}")
        )
    }
}
