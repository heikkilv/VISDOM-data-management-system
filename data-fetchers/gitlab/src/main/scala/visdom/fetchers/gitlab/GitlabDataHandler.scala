package visdom.fetchers.gitlab

import org.bson.BsonArray
import org.bson.BSONException
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.Document
import scala.collection.JavaConverters.asScalaBufferConverter
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.database.mongodb.MongoConnection


abstract class GitlabDataHandler() {
    def getRequest(): HttpRequest

    def processDocument(document: BsonDocument): BsonDocument = {
        document
    }

    def getCollection(): Option[MongoCollection[Document]] = {
        None
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
            utils.HttpUtils.makeRequest(requestInternal) match {
                case Some(response: HttpResponse[String]) => response.code match {
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
                case None => resultDocuments
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
        results.isEmpty match {
            case true => None
            case false => Some(results)
        }
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
}
