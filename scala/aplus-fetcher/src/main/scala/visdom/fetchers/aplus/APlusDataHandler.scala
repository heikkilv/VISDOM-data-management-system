package visdom.fetchers.aplus

import java.time.Instant
import java.util.concurrent.TimeoutException
import org.bson.BsonDocument
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonElement
import org.mongodb.scala.bson.BsonInt32
import org.mongodb.scala.bson.Document
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.concurrent.Await
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import visdom.fetchers.DataHandler
import visdom.http.HttpConstants
import visdom.http.HttpUtils
import visdom.json.JsonUtils.EnrichedBsonDocument


abstract class APlusDataHandler(options: APlusFetchOptions)
extends DataHandler(options) {
    def usePagination(): Boolean

    def handleRequests(firstRequest: HttpRequest): Option[Array[Document]] = {
        def handleRequestInternal(
            requestInternal: HttpRequest,
            resultDocuments: Array[Document]
        ): Array[Document] = {
            try {
                Await.result(
                    visdom.http.HttpUtils.makeRequest(requestInternal),
                    APlusConstants.DefaultWaitDuration
                ) match {
                    case Some(response: HttpResponse[String]) => response.code match {
                        case HttpConstants.StatusCodeOk => {
                            val receivedResults: Array[Document] = resultDocuments ++ processResponse(response)
                            usePagination() match {
                                case true => getNextRequest(requestInternal, response) match {
                                    case Some(nextRequest: HttpRequest) =>
                                        handleRequestInternal(nextRequest, receivedResults)
                                    case None => receivedResults
                                }
                                case false => receivedResults
                            }
                        }
                        case _ => resultDocuments
                    }
                    case None => resultDocuments
                }
            } catch  {
                case _: TimeoutException => resultDocuments
            }
        }

        handleResults(handleRequestInternal(firstRequest, Array()))
    }

    protected def getMetadataBase(): BsonDocument = {
        new BsonDocument(
            List(
                new BsonElement(
                    APlusConstants.AttributeLastModified,
                    new BsonDateTime(Instant.now().toEpochMilli())
                ),
                new BsonElement(
                    APlusConstants.AttributeApiVersion,
                    new BsonInt32(APlusConstants.APlusApiVersion)
                )
            ).asJava
        )
    }

    private def getNextRequest(
        request: HttpRequest,
        response: HttpResponse[String]
    ): Option[HttpRequest] = {
        HttpUtils.responseToDocument(response) match {
            case Some(document: BsonDocument) => document.getStringOption(APlusConstants.AttributeNext) match {
                case Some(nextUri: String) => Some(Http(nextUri).headers(request.headers))
                case None => None
            }
            case None => None
        }
    }
}
