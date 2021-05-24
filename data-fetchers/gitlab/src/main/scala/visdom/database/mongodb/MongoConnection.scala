package visdom.database.mongodb

import io.circe.JsonObject
import org.mongodb.scala.Document
import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoClientSettings
import org.mongodb.scala.MongoCredential
import org.mongodb.scala.ServerAddress
import org.mongodb.scala.SingleObservable
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.result.InsertManyResult
import scala.collection.JavaConverters.seqAsJavaListConverter
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.MongoCollection
import visdom.utils.json.Conversions.jsonObjectsToBson


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
    val mongoClient: MongoClient = MongoClient(
        MongoClientSettings.builder()
            .applicationName(applicationName)
            .applyToClusterSettings(
                (builder: ClusterSettings.Builder) => builder.hosts(
                    List(mongoServerAddress).asJava
                )
            )
            .credential(mongoCredentials)
            .build()
    )

    def insertData(
        databaseName: String,
        collectionName: String,
        dataObjects: Vector[JsonObject]
    ): SingleObservable[InsertManyResult] = {
        val database: MongoDatabase = mongoClient.getDatabase(databaseName)
        val collection: MongoCollection[Document] = database.getCollection(collectionName)
        val bsonDocuments: Vector[Document] = jsonObjectsToBson(dataObjects)
        collection.insertMany(bsonDocuments)
    }
}
