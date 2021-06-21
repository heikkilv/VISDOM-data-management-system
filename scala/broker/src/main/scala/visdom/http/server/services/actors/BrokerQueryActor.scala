package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.ExecutionContext
import visdom.database.mongodb.MongoConnection
import visdom.http.server.QueryOptionsBaseObject
import visdom.http.server.ServerConstants
import visdom.http.server.response.ComponentInfoResponse
import visdom.utils.WartRemoverConstants
import visdom.http.server.BrokerQueryOptions
import visdom.http.server.response.BaseResponse
import visdom.http.server.response.FetchersResponse
import spray.json.JsObject
import visdom.broker.Metadata
import visdom.constants.ComponentConstants
import visdom.http.server.services.constants.BrokerDescriptions
import visdom.utils.MetadataConstants
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.collection.immutable.Document


class BrokerQueryActor extends Actor with ActorLogging {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case BrokerQueryOptions(componentType: String) => {
            log.info(BrokerDescriptions.BrokerQueryLogEntry + componentType)
            val componentsOption: Option[Document] = MongoConnection.getDocuments(
                MongoConnection.getMainMetadataCollection(),
                Map(MetadataConstants.AttributeComponentType -> BsonString(componentType))
            )
            componentsOption match {
                case Some(components: Document) => println(components.toJson())
                case None => println("None")
            }
            sender() ! Metadata.getInfoResponse()
        }
    }
}
