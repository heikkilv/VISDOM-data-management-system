package visdom.fetchers.gitlab

import java.time.Instant
import java.util.concurrent.TimeoutException
import org.bson.BsonArray
import org.bson.BSONException
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonInt64
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.Document
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.concurrent.Await
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConnection
import visdom.database.mongodb.MongoConstants
import visdom.fetchers.gitlab.utils.JsonUtils.EnrichedBsonDocument
import visdom.fetchers.gitlab.utils.JsonUtils.toBsonValue


abstract class GitlabDataHandler(options: GitlabFetchOptions) {
    def getFetcherType(): String
    def getCollectionName(): String
    def getRequest(): HttpRequest

    def getOptionsDocument(): BsonDocument = {
        BsonDocument()
    }

    def processDocument(document: BsonDocument): BsonDocument = {
        document
    }

    def getIdentifierAttributes(): Array[String] = {
        Array(GitlabConstants.AttributeId)
    }

    def processResponse(response: HttpResponse[String]): Array[Document] = {
        try {
            // all valid responses from GitLab API should be JSON arrays containing JSON objects
            BsonArray.parse(response.body)
                .getValues()
                .asScala
                .toArray
                .map(bsonValue => bsonValue.isDocument match {
                    case true => Some(bsonValue.asDocument())
                    case false => None
                })
                .flatten
                .map(document => {
                    val finalDocument: Document = Document(processDocument(document))

                    // store the fetched documents to MongoDB
                    getCollection() match {
                        case Some(collection: MongoCollection[Document]) => MongoConnection.storeDocument(
                            collection,
                            finalDocument,
                            getIdentifierAttributes()
                        )
                        case None =>
                    }

                    finalDocument
                })
        }
        catch {
            case error: BSONException => {
                println(error.getMessage())
                Array()
            }
        }
    }

    def handleRequests(firstRequest: HttpRequest): Option[Array[Document]] = {
        def handleRequestInternal(
            requestInternal: HttpRequest,
            resultDocuments: Array[Document],
            page: Int
        ): Array[Document] = {
            def responseHelper(response: HttpResponse[String]): Array[Document] = {
                response.code match {
                    case GitlabConstants.StatusCodeOk => {
                        val resultArray: Array[Document] = processResponse(response)
                        val allResults: Array[Document] = resultDocuments ++ resultArray
                        getNextRequest(requestInternal, response, page) match {
                            case Some(nextRequest: HttpRequest) =>
                                handleRequestInternal(nextRequest, allResults, page + 1)
                            case None => allResults
                        }
                    }
                    case _ => resultDocuments
                }
            }

            try {
                Await.result(
                    utils.HttpUtils.makeRequest(requestInternal),
                    GitlabConstants.DefaultWaitDuration
                ) match {
                    case Some(response: HttpResponse[String]) => responseHelper(response)
                    case None => resultDocuments
                }
            } catch  {
                case _: TimeoutException => resultDocuments
            }
        }

        val requestWithPageParams: HttpRequest = firstRequest.params(
            (GitlabConstants.ParamPerPage, GitlabConstants.DefaultPerPage.toString()),
            (GitlabConstants.ParamPage, GitlabConstants.DefaultStartPage.toString())
        )
        val results: Array[Document] = handleRequestInternal(
            requestWithPageParams,
            Array(),
            GitlabConstants.DefaultStartPage
        )
        handleResults(results)
    }

    final def getCollection(): Option[MongoCollection[Document]] = {
        options.mongoDatabase match {
            case Some(database: MongoDatabase) => Some(
                database.getCollection(getCollectionName())
            )
            case None => None
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
                GitlabConstants.AttributeType -> BsonString(getFetcherType()),
                GitlabConstants.AttributeHostName -> BsonString(options.hostServer.hostName)
            )
            .appendOption(GitlabConstants.AttributeProjectName, (options match {
                case GitlabCommitOptions(_, _, projectName, _, _, _, _, _, _, _) =>
                    Some(toBsonValue(projectName))
                case GitlabFileOptions(_, _, projectName, _, _, _, _) =>
                    Some(toBsonValue(projectName))
                case GitlabCommitLinkOptions(_, _, projectName, _) =>
                    Some(toBsonValue(projectName))
                case _ => None
            }))
            .append(GitlabConstants.AttributeDocumentUpdatedCount, BsonInt32(results match {
                case Some(resultArray: Array[Document]) => resultArray.size
                case None => 0
            }))
            .append(GitlabConstants.AttributeTimestamp, BsonDateTime(Instant.now().toEpochMilli()))
            .append(GitlabConstants.AttributeOptions, getOptionsDocument())
        )
    }

    def process(): Option[Array[Document]] = {
        handleRequests(getRequest())
    }

    private def getNextRequest(
        request: HttpRequest,
        response: HttpResponse[String],
        currentPage: Int
    ): Option[HttpRequest] = {
        response.header(GitlabConstants.HeaderNextPage) match {
            case Some(nextPageValue: String) => utils.GeneralUtils.toInt(nextPageValue) match {
                case Some(nextPage: Int) if (nextPage == currentPage + 1) => {
                    val nextRequest: HttpRequest = utils.HttpUtils.replaceRequestParam(
                        request,
                        GitlabConstants.ParamPage,
                        nextPageValue
                    )
                    Some(nextRequest)
                }
                case _ => None
            }
            case None => None
        }
    }

    private def handleResults(results: Array[Document]): Option[Array[Document]] = {
        val resultsOption: Option[Array[Document]] = results.isEmpty match {
            case true => None
            case false => Some(results)
        }

        // store a metadata document to MongoDB
        getMetadataCollection() match {
            case Some(metadataCollection: MongoCollection[Document]) => MongoConnection.storeDocument(
                metadataCollection,
                getMetadataDocument(resultsOption),
                Array()
            )
            case None =>
        }

        resultsOption
    }
}
