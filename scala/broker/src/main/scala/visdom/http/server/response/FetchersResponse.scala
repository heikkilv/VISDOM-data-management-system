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
                AttributeConstants.AttributeComponentName -> JsString(componentName),
                AttributeConstants.AttributeFetcherType -> JsString(fetcherType),
                AttributeConstants.AttributeVersion -> JsString(version),
                AttributeConstants.AttributeApiAddress -> JsString(apiAddress),
                AttributeConstants.AttributeSwaggerDefinition -> JsString(swaggerDefinition),
                AttributeConstants.AttributeInformation -> information
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
                    information = fetcherType match {
                        case ComponentConstants.GitlabFetcherType => {
                            documentKeys.intersect(GitlabFetcherInformation.requiredKeys).size match {
                                case n: Int if n < GitlabFetcherInformation.requiredKeys.size => JsObject()
                                case _ => GitlabFetcherInformation(
                                    gitlabServer = document.getString(SnakeCaseConstants.GitlabServer),
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
