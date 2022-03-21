package visdom.adapters.general.model.events.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.schemas.SubmissionGitDataSchema
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class SubmissionGitContent(
    host_name: String,
    project_name: String
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.HostName -> JsonUtils.toBsonValue(host_name),
                SnakeCaseConstants.ProjectName -> JsonUtils.toBsonValue(project_name)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.HostName -> JsonUtils.toJsonValue(host_name),
                SnakeCaseConstants.ProjectName -> JsonUtils.toJsonValue(project_name)
            )
        )
    }
}

object SubmissionGitContent {
    def fromSubmissionGitData(gitData: SubmissionGitDataSchema): SubmissionGitContent = {
        SubmissionGitContent(gitData.host_name, gitData.project_name)
    }
}
