// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http

import java.util.concurrent.TimeoutException
import org.bson.BsonArray
import org.bson.BsonDocument
import org.bson.BSONException
import org.bson.json.JsonParseException
import scalaj.http.Http
import scalaj.http.HttpRequest
import scalaj.http.HttpResponse
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future


object HttpUtils {
    implicit val ec: ExecutionContext = ExecutionContext.global

    def getSimpleRequest(targetUrl: String): HttpRequest = {
        Http(targetUrl)
    }

    def makeRequest(inputRequest: HttpRequest): Future[Option[HttpResponse[String]]] = {
        Future(
            try {
                Some(inputRequest.asString)
            }
            catch {
                case ioException: java.io.IOException => {
                    println(ioException)
                    None
                }
            }
        )
    }

    // adds or replaces the given query parameter to the GET request
    def replaceRequestParam(request: HttpRequest, paramName: String, paramValue: String): HttpRequest = {
        val cleanedParams: Seq[(String, String)] = request.params.filter(param => param._1 != paramName)

        Http(request.url)
            .charset(request.charset)
            .headers(request.headers)
            .params(cleanedParams ++ Seq((paramName, paramValue)))
            .options(request.options)
    }

    def getRequestResponse(request: HttpRequest): Option[HttpResponse[String]] = {
        try {
            Await.result(
                makeRequest(request),
                HttpConstants.DefaultWaitDuration
            )
        } catch {
             case timeoutException: TimeoutException => {
                println(timeoutException)
                None
             }
        }
    }

    def returnRequestStatusCode(request: HttpRequest): Int = {
        getRequestResponse(request) match {
            case Some(response: HttpResponse[String]) => response.code
            case None => HttpConstants.StatusCodeUnknown
        }
    }

    def getRequestBody(request: HttpRequest, expectedStatusCode: Int): Option[String] = {
        getRequestResponse(request) match {
            case Some(response: HttpResponse[String]) => {
                response.code match {
                    case code: Int if code == expectedStatusCode => Some(response.body)
                    case _ => None
                }
            }
            case None => None
        }
    }

    def getRequestDocument(request: HttpRequest, expectedStatusCode: Int): Option[BsonDocument] = {
        getRequestBody(request, expectedStatusCode) match {
            case Some(responseBody: String) => {
                try {
                    Some(BsonDocument.parse(responseBody))
                }
                catch {
                    case _: BSONException => None
                }
            }
            case None => None
        }
    }

    def bsonArrayToDocumentArray(bsonArray: BsonArray): Array[BsonDocument] = {
        bsonArray.getValues()
            .asScala
            .toArray
            .map(bsonValue => bsonValue.isDocument match {
                case true => Some(bsonValue.asDocument())
                case false => None
            })
            .flatten
    }

    def responseToDocument(response: HttpResponse[String]): Option[BsonDocument] = {
        try {
            Some(BsonDocument.parse(response.body))
        }
        catch {
            case _: JsonParseException => None
        }
    }

    def responseToDocumentArrayCaseArray(response: HttpResponse[String]): Array[BsonDocument] = {
        responseToDocumentArrayCaseArray(response.body)
    }

    def responseToDocumentArrayCaseArray(responseString: String): Array[BsonDocument] = {
        bsonArrayToDocumentArray(
            try {
                BsonArray.parse(responseString)
            }
            catch {
                case _: JsonParseException => new BsonArray()
            }
        )
    }

    def responseToDocumentArrayCaseDocument(response: HttpResponse[String]): Array[BsonDocument] = {
        try {
            Array(BsonDocument.parse(response.body))
        }
        catch {
            case _: JsonParseException => Array()
        }
    }

    def responseToDocumentArrayCaseAttributeDocument(
        response: HttpResponse[String],
        documentAttribute: String
    ): Array[BsonDocument] = {
        bsonArrayToDocumentArray(
            try {
                val parsedDocument: BsonDocument = BsonDocument.parse(response.body)
                parsedDocument.containsKey(documentAttribute) match {
                    case true => parsedDocument.get(documentAttribute) match {
                        case documentArray: BsonArray => documentArray
                        case _ => new BsonArray()
                    }
                    case false => new BsonArray()
                }
            }
            catch {
                case _: JsonParseException => new BsonArray()
            }
        )
    }
}
