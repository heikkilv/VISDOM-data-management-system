// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.artifacts.data


import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.FileLinksSchema
import visdom.adapters.general.schemas.FileSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class FileData(
    file_id: String,
    `type`: String,
    mode: String,
    commits: Seq[String]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.FileId -> JsonUtils.toBsonValue(file_id),
                SnakeCaseConstants.Type -> JsonUtils.toBsonValue(`type`),
                SnakeCaseConstants.Mode -> JsonUtils.toBsonValue(mode),
                SnakeCaseConstants.Commits -> JsonUtils.toBsonValue(commits)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.FileId -> JsonUtils.toJsonValue(file_id),
                SnakeCaseConstants.Type -> JsonUtils.toJsonValue(`type`),
                SnakeCaseConstants.Mode -> JsonUtils.toJsonValue(mode),
                SnakeCaseConstants.Commits -> JsonUtils.toJsonValue(commits)
            )
        )
    }
}

object FileData {
    def fromFileSchema(fileSchema: FileSchema): FileData = {
        FileData(
            file_id = fileSchema.id,
            `type` = fileSchema.`type`,
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
