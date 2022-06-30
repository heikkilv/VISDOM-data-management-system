// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.response

import spray.json.JsObject
import spray.json.JsString
import spray.json.JsValue
import org.mongodb.scala.bson.collection.immutable.Document
import visdom.constants.ComponentConstants
import visdom.utils.AttributeConstants
import visdom.utils.CommonConstants.EmptyString
import visdom.utils.SnakeCaseConstants


final case class AdaptersResponse(
    componentName: String,
    adapterType: String,
    version: String,
    apiAddress: String,
    swaggerDefinition: String,
    information: JsObject
) extends ComponentsResponse {
    def toJsObject(): JsObject = {
        JsObject({
            Map[String, JsValue](
                AttributeConstants.ComponentName -> JsString(componentName),
                AttributeConstants.AdapterType -> JsString(adapterType),
                AttributeConstants.Version -> JsString(version),
                AttributeConstants.ApiAddress -> JsString(apiAddress),
                AttributeConstants.SwaggerDefinition -> JsString(swaggerDefinition),
                AttributeConstants.Information -> information
            )
        })
    }
}

object AdaptersResponse {
    def requiredKeys: Set[String] = Set(
        SnakeCaseConstants.ApplicationName,
        SnakeCaseConstants.AdapterType,
        SnakeCaseConstants.Version,
        SnakeCaseConstants.ApiAddress,
        SnakeCaseConstants.SwaggerDefinition
    )

    def fromDocument(document: Document): Option[AdaptersResponse] = {
        val documentKeys: Set[String] = document.keySet

        documentKeys.intersect(requiredKeys).size match {
            case m: Int if m < requiredKeys.size => None
            case _ => Some({
                val adapterType: String =
                    document.getString(SnakeCaseConstants.AdapterType)
                AdaptersResponse(
                    adapterType = adapterType,
                    componentName =
                        document.getString(SnakeCaseConstants.ApplicationName),
                    version =
                        document.getString(SnakeCaseConstants.Version),
                    apiAddress =
                        document.getString(SnakeCaseConstants.ApiAddress),
                    swaggerDefinition =
                        document.getString(SnakeCaseConstants.SwaggerDefinition),
                    information = adapterType match {
                        case ComponentConstants.GitlabAdapterType => {
                            documentKeys.intersect(GitlabAdapterInformation.requiredKeys).size match {
                                case n: Int if n < GitlabAdapterInformation.requiredKeys.size => JsObject()
                                case _ => GitlabAdapterInformation(
                                    database = document.getString(SnakeCaseConstants.Database)
                                ).toJsObject()
                            }
                        }
                        case _ => JsObject()
                    }
                )
            })
        }
    }
}
