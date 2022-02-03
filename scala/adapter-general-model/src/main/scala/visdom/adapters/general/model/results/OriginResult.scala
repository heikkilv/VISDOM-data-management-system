package visdom.adapters.general.model.results

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.model.base.Origin
import visdom.adapters.general.model.origins.GitlabOrigin
import visdom.adapters.general.model.origins.data.GitlabOriginData
import visdom.adapters.general.schemas.GitlabProjectSchemaTrait
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class OriginResult[OriginData <: Data](
    id: String,
    originType: String,
    source: String,
    context: String,
    data: OriginData
)
extends BaseResultValue {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toBsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(originType),
                SnakeCaseConstants.Source -> JsonUtils.toBsonValue(source),
                SnakeCaseConstants.Context -> JsonUtils.toBsonValue(context),
                SnakeCaseConstants.Data -> data.toBsonValue()
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(originType),
                SnakeCaseConstants.Source -> JsonUtils.toJsonValue(source),
                SnakeCaseConstants.Context -> JsonUtils.toJsonValue(context),
                SnakeCaseConstants.Data -> data.toJsValue()
            )
        )
    }
}

object OriginResult {
    type GitlabOriginResult = OriginResult[GitlabOriginData]

    def fromOrigin[OriginData <: Data](origin: Origin, originData: OriginData): OriginResult[OriginData] = {
        OriginResult(
            id = origin.id,
            originType = origin.getType,
            source = origin.source,
            context = origin.context,
            data = originData
        )
    }

    def fromGitlabProjectSchema(gitlabProjectSchema: GitlabProjectSchemaTrait): GitlabOriginResult = {
        val gitlabOrigin: GitlabOrigin = new GitlabOrigin(
            hostName = gitlabProjectSchema.host_name,
            projectGroup = gitlabProjectSchema.group_name,
            projectName = gitlabProjectSchema.project_name
        )
        fromOrigin(gitlabOrigin, gitlabOrigin.data)
    }
}
