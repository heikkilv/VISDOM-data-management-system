// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.fetchers

import java.time.Instant
import org.bson.BsonArray
import org.bson.BSONException
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.Document
import scala.collection.JavaConverters.asScalaBufferConverter
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConnection
import visdom.database.mongodb.MongoConstants
import visdom.json.JsonUtils.EnrichedBsonDocument
import visdom.utils.AttributeConstants
import visdom.utils.CommonConstants


abstract class DataHandler(options: FetchOptions) {
    def getFetcherType(): String
    def getCollectionName(): String
    def getRequest(): HttpRequest
    def handleRequests(firstRequest: HttpRequest): Option[Array[Document]]
    def responseToDocumentArray(response: HttpResponse[String]): Array[BsonDocument]

    val createMetadataDocument: Boolean = true

    def getOptions(): FetchOptions = options

    def getOptionsDocument(): BsonDocument = {
        BsonDocument()
    }

    def processDocument(document: BsonDocument): BsonDocument = {
        document
    }

    def getIdentifierAttributes(): Array[String] = {
        Array(AttributeConstants.Id)
    }

    def getHashableAttributes(): Option[Seq[Seq[String]]] = None

    def getProjectName(): Option[String] = None

    def processResponse(response: HttpResponse[String]): Array[Document] = {
        try {
            responseToDocumentArray(response)
                .map(document => {
                    val finalDocument: Document = Document(
                        processDocument(document).anonymize(getHashableAttributes())
                    )

                    if (finalDocument.nonEmpty) {
                        // store the fetched documents to MongoDB
                        getCollection() match {
                            case Some(collection: MongoCollection[Document]) => MongoConnection.storeDocument(
                                collection,
                                finalDocument,
                                getIdentifierAttributes()
                            )
                            case None =>
                        }
                    }

                    finalDocument
                })
            .filter(document => document.nonEmpty)
        }
        catch {
            case error: BSONException => {
                println(error.getMessage())
                Array()
            }
        }
    }

    final def getCollection(): Option[MongoCollection[Document]] = {
        val collectionName: String = getCollectionName()
        collectionName match {
            case CommonConstants.EmptyString => None
            case _ => options.mongoDatabase match {
                case Some(database: MongoDatabase) => Some(
                    database.getCollection(collectionName)
                )
                case None => None
            }
        }
    }

    final def getMetadataCollection(): Option[MongoCollection[Document]] = {
        options.mongoDatabase match {
            case Some(database: MongoDatabase) => Some(
                database.getCollection(MongoConstants.CollectionMetadata)
            )
            case None => None
        }
    }

    protected def getMetadataDocument(results: Option[Array[Document]]): Document = {
        Document(
            BsonDocument(
                MongoConstants.AttributeType -> BsonString(getFetcherType()),
                MongoConstants.AttributeHostName -> BsonString(options.hostServer.hostName)
            )
            .appendOption(MongoConstants.AttributeProjectName, getProjectName() match {
                case Some(projectName: String) => Some(BsonString(projectName))
                case None => None
            })
            .append(MongoConstants.AttributeDocumentUpdatedCount, BsonInt32(results match {
                case Some(resultArray: Array[Document]) => resultArray.size
                case None => 0
            }))
            .append(MongoConstants.AttributeTimestamp, BsonDateTime(Instant.now().toEpochMilli()))
            .append(MongoConstants.AttributeOptions, getOptionsDocument())
        )
    }

    def process(): Option[Array[Document]] = {
        handleRequests(getRequest())
    }

    protected def handleResults(results: Array[Document]): Option[Array[Document]] = {
        val resultsOption: Option[Array[Document]] = results.isEmpty match {
            case true => None
            case false => Some(results)
        }

        // store a metadata document to MongoDB
        createMetadataDocument match {
            case true => getMetadataCollection() match {
                case Some(metadataCollection: MongoCollection[Document]) => MongoConnection.storeDocument(
                    metadataCollection,
                    getMetadataDocument(resultsOption),
                    Array()
                )
                case None =>
            }
            case false =>
        }

        resultsOption
    }
}
