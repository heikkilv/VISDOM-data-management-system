package visdom.adapters.general.model.artifacts.data


import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.FileLinksSchema
import visdom.adapters.general.schemas.FileSchema
import visdom.adapters.results.BaseResultValue
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class FileData(
    fileId: String,
    fileType: String,
    mode: String,
    commits: Seq[String]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.FileId -> JsonUtils.toBsonValue(fileId),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(fileType),
                SnakeCaseConstants.Mode -> JsonUtils.toBsonValue(mode),
                SnakeCaseConstants.Commits -> JsonUtils.toBsonValue(commits)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.FileId -> JsonUtils.toJsonValue(fileId),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(fileType),
                SnakeCaseConstants.Mode -> JsonUtils.toJsonValue(mode),
                SnakeCaseConstants.Commits -> JsonUtils.toJsonValue(commits)
            )
        )
    }
}

object FileData {
    def fromFileSchema(fileSchema: FileSchema): FileData = {
        FileData(
            fileId = fileSchema.id,
            fileType = fileSchema.`type`,
            mode = fileSchema.mode,
            commits = fileSchema._links match {
                case Some(links: FileLinksSchema) => links.commits match {
                    case Some(commitLinks: Seq[String]) => commitLinks
                    case None => Seq.empty
                }
                case None => Seq.empty
            }
        )
    }
}
