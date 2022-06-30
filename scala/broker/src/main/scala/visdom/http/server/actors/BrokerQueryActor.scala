// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.http.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import java.time.Instant
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.BsonString
import org.mongodb.scala.bson.collection.immutable.Document
import spray.json.JsArray
import visdom.constants.ComponentConstants
import visdom.database.mongodb.MongoConnection
import visdom.http.server.BrokerQueryOptions
import visdom.http.server.response.AdaptersResponse
import visdom.http.server.response.ComponentsResponse
import visdom.http.server.response.FetchersResponse
import visdom.http.server.services.constants.BrokerDescriptions
import visdom.utils.MetadataConstants
import visdom.utils.WartRemoverConstants
import visdom.http.server.response.JsonArrayResponse


class BrokerQueryActor extends Actor with ActorLogging {
    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def receive: Receive = {
        case BrokerQueryOptions(componentType: String) => {
            log.info(BrokerDescriptions.BrokerQueryLogEntry + componentType)

            val componentList: List[Document] = MongoConnection.getDocuments(
                MongoConnection.getMainMetadataCollection(),
                List(
                    MongoConnection.getEqualFilter(
                        MetadataConstants.AttributeComponentType,
                        BsonString(componentType)
                    ),
                    MongoConnection.getGreaterThanFilter(
                        MetadataConstants.AttributeTimestamp,
                        BsonDateTime(Instant.now().toEpochMilli() - MetadataConstants.ComponentActiveInterval)
                    )
                )
            )

            val responsesOption: List[Option[ComponentsResponse]] = componentType match {
                case ComponentConstants.AdapterComponentType => componentList.map(
                    document => AdaptersResponse.fromDocument(document)
                )
                case ComponentConstants.FetcherComponentType =>
                    componentList.map(document => FetchersResponse.fromDocument(document))
            }
            val responses: List[ComponentsResponse] = responsesOption.flatten

            sender() ! JsonArrayResponse(
                JsArray(
                    responses.map(component => component.toJsObject()).toVector
                )
            )
        }
    }
}
