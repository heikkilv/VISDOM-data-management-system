package visdom.adapters.general.model.origins.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class GitlabOriginData(
    project_id: Option[Int],
    group_name: String
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.ProjectId -> JsonUtils.toBsonValue(project_id),
                SnakeCaseConstants.GroupName -> JsonUtils.toBsonValue(group_name)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.ProjectId -> JsonUtils.toJsonValue(project_id),
                SnakeCaseConstants.GroupName -> JsonUtils.toJsonValue(group_name)
            )
        )
    }
}
