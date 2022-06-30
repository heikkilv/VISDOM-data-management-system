// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.response

import org.mongodb.scala.bson.collection.immutable.Document
import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import visdom.constants.ComponentConstants
import visdom.utils.AttributeConstants
import visdom.utils.CommonConstants.EmptyString
import visdom.utils.SnakeCaseConstants


final case class FetchersResponse(
    componentName: String,
    fetcherType: String,
    version: String,
    apiAddress: String,
    swaggerDefinition: String,
    information: JsObject
) extends ComponentsResponse {
    def toJsObject(): JsObject = {
        JsObject({
            Map[String, JsValue](
                AttributeConstants.ComponentName -> JsString(componentName),
                AttributeConstants.FetcherType -> JsString(fetcherType),
                AttributeConstants.Version -> JsString(version),
                AttributeConstants.ApiAddress -> JsString(apiAddress),
                AttributeConstants.SwaggerDefinition -> JsString(swaggerDefinition),
                AttributeConstants.Information -> information
            )
        })
    }
}

object FetchersResponse {
    def requiredKeys: Set[String] = Set(
        SnakeCaseConstants.ApplicationName,
        SnakeCaseConstants.FetcherType,
        SnakeCaseConstants.Version,
        SnakeCaseConstants.ApiAddress,
        SnakeCaseConstants.SwaggerDefinition
    )

    def fromDocument(document: Document): Option[FetchersResponse] = {
        val documentKeys: Set[String] = document.keySet

        def getFetcherInformation(fetcherType: String) = {
            fetcherType match {
                case ComponentConstants.GitlabFetcherType => {
                    documentKeys.intersect(GitlabFetcherInformation.requiredKeys).size match {
                        case n: Int if n < GitlabFetcherInformation.requiredKeys.size => JsObject()
                        case _ => GitlabFetcherInformation(
                            sourceServer = document.getString(SnakeCaseConstants.SourceServer),
                            database = document.getString(SnakeCaseConstants.Database)
                        ).toJsObject()
                    }
                }
                case ComponentConstants.APlusFetcherType => {
                    documentKeys.intersect(APlusFetcherInformation.requiredKeys).size match {
                        case n: Int if n < APlusFetcherInformation.requiredKeys.size => JsObject()
                        case _ => APlusFetcherInformation(
                            sourceServer = document.getString(SnakeCaseConstants.SourceServer),
                            database = document.getString(SnakeCaseConstants.Database)
                        ).toJsObject()
                    }
                }
                case _ => JsObject()
            }
        }

        documentKeys.intersect(requiredKeys).size match {
            case m: Int if m < requiredKeys.size => None
            case _ => Some({
                val fetcherType: String =
                    document.getString(SnakeCaseConstants.FetcherType)
                FetchersResponse(
                    fetcherType = fetcherType,
                    componentName =
                        document.getString(SnakeCaseConstants.ApplicationName),
                    version =
                        document.getString(SnakeCaseConstants.Version),
                    apiAddress =
                        document.getString(SnakeCaseConstants.ApiAddress),
                    swaggerDefinition =
                        document.getString(SnakeCaseConstants.SwaggerDefinition),
                    information = getFetcherInformation(fetcherType)
                )
            })
        }
    }
}
