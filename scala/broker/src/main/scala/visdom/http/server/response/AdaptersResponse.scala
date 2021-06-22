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
                AttributeConstants.AttributeComponentName -> JsString(componentName),
                AttributeConstants.AttributeAdapterType -> JsString(adapterType),
                AttributeConstants.AttributeVersion -> JsString(version),
                AttributeConstants.AttributeApiAddress -> JsString(apiAddress),
                AttributeConstants.AttributeSwaggerDefinition -> JsString(swaggerDefinition),
                AttributeConstants.AttributeInformation -> information
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
