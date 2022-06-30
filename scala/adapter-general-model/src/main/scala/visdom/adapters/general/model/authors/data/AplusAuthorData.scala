// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.authors.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.AplusUserSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class AplusAuthorData(
    user_id: Int,
    username: String,
    student_id: String,
    email: String,
    is_external: Boolean
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.UserId -> JsonUtils.toBsonValue(user_id),
                SnakeCaseConstants.Username -> JsonUtils.toBsonValue(username),
                SnakeCaseConstants.StudentId -> JsonUtils.toBsonValue(student_id),
                SnakeCaseConstants.Email -> JsonUtils.toBsonValue(email),
                SnakeCaseConstants.IsExternal -> JsonUtils.toBsonValue(is_external)
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.UserId -> JsonUtils.toJsonValue(user_id),
                SnakeCaseConstants.Username -> JsonUtils.toJsonValue(username),
                SnakeCaseConstants.StudentId -> JsonUtils.toJsonValue(student_id),
                SnakeCaseConstants.Email -> JsonUtils.toJsonValue(email),
                SnakeCaseConstants.IsExternal -> JsonUtils.toJsonValue(is_external)
            )
        )
    }
}

object AplusAuthorData {
    def fromAplusUserSchema(aplusUserSchema: AplusUserSchema): AplusAuthorData = {
        AplusAuthorData(
            user_id = aplusUserSchema.id,
            username = aplusUserSchema.username,
            student_id = aplusUserSchema.student_id,
            email = aplusUserSchema.email,
            is_external = aplusUserSchema.is_external
        )
    }
}
