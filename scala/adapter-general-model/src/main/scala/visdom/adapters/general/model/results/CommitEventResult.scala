package visdom.adapters.general.model.results

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.ItemLink
import visdom.adapters.general.model.events.data.CommitData
import visdom.adapters.general.model.events.CommitEvent
import visdom.adapters.general.schemas.CommitSchema
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.GeneralUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class CommitEventResult(
    id: String,
    event_type: String,
    time: String,
    duration: Double,
    message: String,
    origin: ItemLink,
    author: ItemLink,
    data: CommitData,
    relatedConstructs: Seq[ItemLink],
    relatedEvents: Seq[ItemLink]
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(event_type),
                SnakeCaseConstants.Time -> JsonUtils.toBsonValue(time),
                SnakeCaseConstants.Duration -> JsonUtils.toBsonValue(duration),
                SnakeCaseConstants.Message -> JsonUtils.toBsonValue(message),
                SnakeCaseConstants.Origin -> origin.toBsonValue(),
                SnakeCaseConstants.Author -> author.toBsonValue(),
                SnakeCaseConstants.Data -> data.toBsonValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toBsonValue(relatedConstructs.map(link => link.toBsonValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toBsonValue(relatedEvents.map(link => link.toBsonValue())),
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(event_type),
                SnakeCaseConstants.Time -> JsonUtils.toJsonValue(time),
                SnakeCaseConstants.Duration -> JsonUtils.toJsonValue(duration),
                SnakeCaseConstants.Message -> JsonUtils.toJsonValue(message),
                SnakeCaseConstants.Origin -> origin.toJsValue(),
                SnakeCaseConstants.Author -> author.toJsValue(),
                SnakeCaseConstants.Data -> data.toJsValue(),
                SnakeCaseConstants.RelatedConstructs ->
                    JsonUtils.toJsonValue(relatedConstructs.map(link => link.toJsValue())),
                SnakeCaseConstants.RelatedEvents ->
                    JsonUtils.toJsonValue(relatedEvents.map(link => link.toJsValue())),
            )
        )
    }
}

object CommitEventResult {
    def fromCommitEvent(commitEvent: CommitEvent): CommitEventResult = {
        CommitEventResult(
            id = commitEvent.id,
            event_type = commitEvent.getType,
            time = commitEvent.time.toString(),
            duration = commitEvent.duration,
            message = commitEvent.message,
            origin = commitEvent.origin,
            author = commitEvent.author,
            data = commitEvent.data,
            relatedConstructs = commitEvent.relatedConstructs.map(link => ItemLink.fromLinkTrait(link)),
            relatedEvents = commitEvent.relatedEvents.map(link => ItemLink.fromLinkTrait(link))
        )
    }

    def fromCommitSchema(commitSchema: CommitSchema): CommitEventResult = {
        fromCommitEvent(new CommitEvent(commitSchema))
    }
}
