// Copyright 2022 Tampere University
// This software was developed as a part of the VISDOM project: https://iteavisdom.org/
// This source code is licensed under the MIT license. See LICENSE in the repository root directory.
// Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

package visdom.adapters.general.model.metadata.data

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.BsonValue
import spray.json.JsObject
import spray.json.JsValue
import visdom.adapters.general.model.base.Data
import visdom.adapters.general.schemas.CourseLinksSchema
import visdom.adapters.general.schemas.CourseMetadataOtherSchema
import visdom.adapters.general.schemas.CourseSchema
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.utils.WartRemoverConstants


final case class CourseData(
    course_id: Int,
    code: String,
    instance_name: String,
    url: String,
    html_url: String,
    language: String,
    starting_time: String,
    ending_time: String,
    visible_to_students: Boolean,
    late_submission_coefficient: Option[Double],
    git_branch: Option[String],
    modules: Seq[Int]
)
extends Data {
    def toBsonValue(): BsonValue = {
        BsonDocument(
            Map(
                SnakeCaseConstants.CourseId -> JsonUtils.toBsonValue(course_id),
                SnakeCaseConstants.Code -> JsonUtils.toBsonValue(code),
                SnakeCaseConstants.InstanceName -> JsonUtils.toBsonValue(instance_name),
                SnakeCaseConstants.Url -> JsonUtils.toBsonValue(url),
                SnakeCaseConstants.HtmlUrl -> JsonUtils.toBsonValue(html_url),
                SnakeCaseConstants.Language -> JsonUtils.toBsonValue(language),
                SnakeCaseConstants.StartingTime -> JsonUtils.toBsonValue(starting_time),
                SnakeCaseConstants.EndingTime -> JsonUtils.toBsonValue(ending_time),
                SnakeCaseConstants.VisibleToStudents -> JsonUtils.toBsonValue(visible_to_students),
                SnakeCaseConstants.LateSubmissionCoefficient -> JsonUtils.toBsonValue(late_submission_coefficient),
                SnakeCaseConstants.GitBranch -> JsonUtils.toBsonValue(git_branch),
                SnakeCaseConstants.Modules -> JsonUtils.toBsonValue(modules)
            )
        )
    }

    @SuppressWarnings(Array(WartRemoverConstants.WartsAny))
    def toJsValue(): JsValue = {
        JsObject(
            Map(
                SnakeCaseConstants.CourseId -> JsonUtils.toJsonValue(course_id),
                SnakeCaseConstants.Code -> JsonUtils.toJsonValue(code),
                SnakeCaseConstants.InstanceName -> JsonUtils.toJsonValue(instance_name),
                SnakeCaseConstants.Url -> JsonUtils.toJsonValue(url),
                SnakeCaseConstants.HtmlUrl -> JsonUtils.toJsonValue(html_url),
                SnakeCaseConstants.Language -> JsonUtils.toJsonValue(language),
                SnakeCaseConstants.StartingTime -> JsonUtils.toJsonValue(starting_time),
                SnakeCaseConstants.EndingTime -> JsonUtils.toJsonValue(ending_time),
                SnakeCaseConstants.VisibleToStudents -> JsonUtils.toJsonValue(visible_to_students),
                SnakeCaseConstants.LateSubmissionCoefficient -> JsonUtils.toJsonValue(late_submission_coefficient),
                SnakeCaseConstants.GitBranch -> JsonUtils.toJsonValue(git_branch),
                SnakeCaseConstants.Modules -> JsonUtils.toJsonValue(modules)
            )
        )
    }
}

object CourseData {
    def fromCourseSchema(courseSchema: CourseSchema): CourseData = {
        CourseData(
            course_id = courseSchema.id,
            code = courseSchema.code,
            instance_name = courseSchema.instance_name,
            url = courseSchema.url,
            html_url = courseSchema.html_url,
            language = courseSchema.language,
            starting_time = courseSchema.starting_time,
            ending_time = courseSchema.ending_time,
            visible_to_students = courseSchema.visible_to_students,
            late_submission_coefficient =
                courseSchema.metadata.other.map(other => other.late_submission_coefficient).flatten,
            git_branch = courseSchema.metadata.other.map(other => other.git_branch).flatten,
            modules = courseSchema._links.map(links => links.modules).flatten.getOrElse(Seq.empty)
        )
    }
}
