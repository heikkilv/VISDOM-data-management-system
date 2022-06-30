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
import visdom.adapters.general.schemas.PointsSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants


final case class CoursePointsData(
    course_id: Int,
    user_id: Int,
    submission_count: Int,
    points: Int,
    points_by_difficulty: PointsByDifficulty
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.CourseId -> JsonUtils.toBsonValue(course_id),
                SnakeCaseConstants.UserId -> JsonUtils.toBsonValue(user_id),
                SnakeCaseConstants.SubmissionCount -> JsonUtils.toBsonValue(submission_count),
                SnakeCaseConstants.Points -> JsonUtils.toBsonValue(points),
                SnakeCaseConstants.PointsByDifficulty -> points_by_difficulty.toBsonValue()
            )
        )
    }

    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.CourseId -> JsonUtils.toJsonValue(course_id),
                SnakeCaseConstants.UserId -> JsonUtils.toJsonValue(user_id),
                SnakeCaseConstants.SubmissionCount -> JsonUtils.toJsonValue(submission_count),
                SnakeCaseConstants.Points -> JsonUtils.toJsonValue(points),
                SnakeCaseConstants.PointsByDifficulty -> points_by_difficulty.toJsValue()
            )
        )
    }
}

object CoursePointsData {
    def fromPointsSchema(pointsSchema: PointsSchema): CoursePointsData = {
        CoursePointsData(
            course_id = pointsSchema.course_id,
            user_id = pointsSchema.id,
            submission_count = pointsSchema.submission_count,
            points = pointsSchema.points,
            points_by_difficulty = PointsByDifficulty(
                categoryN = pointsSchema.points_by_difficulty.category,
                categoryP = pointsSchema.points_by_difficulty.categoryP,
                categoryG = pointsSchema.points_by_difficulty.categoryG
            )
        )
    }
}
