package visdom.adapters.course.output

import spray.json.JsObject
import visdom.json.JsonObjectConvertible
import visdom.json.JsonUtils
import visdom.utils.SnakeCaseConstants
import visdom.adapters.course.schemas.PointSchema


final case class StudentCourseOutput(
    id: Int,
    url: String,
    username: String,
    student_id: String,
    email: String,
    full_name: String,
    is_external: Boolean,
    points: CoursePointsOutput,
    commits: Seq[ModuleCommitsOutput]
) extends JsonObjectConvertible {
    def toJsObject(): JsObject = {
        JsObject(
            SnakeCaseConstants.Id -> JsonUtils.toJsonValue(id),
            SnakeCaseConstants.Url -> JsonUtils.toJsonValue(url),
            SnakeCaseConstants.Username -> JsonUtils.toJsonValue(username),
            SnakeCaseConstants.StudentId -> JsonUtils.toJsonValue(student_id),
            SnakeCaseConstants.Email -> JsonUtils.toJsonValue(email),
            SnakeCaseConstants.FullName -> JsonUtils.toJsonValue(full_name),
            SnakeCaseConstants.IsExternal -> JsonUtils.toJsonValue(is_external),
            SnakeCaseConstants.Points -> JsonUtils.toJsonValue(points),
            SnakeCaseConstants.Commits -> JsonUtils.toJsonValue(commits)
        )
    }
}

object StudentCourseOutput {
    def fromSchemas(pointSchema: PointSchema, moduleCommitData: Seq[ModuleCommitsOutput]): StudentCourseOutput = {
        StudentCourseOutput(
            id = pointSchema.id,
            url = pointSchema.url,
            username = pointSchema.username,
            student_id = pointSchema.student_id,
            email = pointSchema.email,
            full_name = pointSchema.full_name,
            is_external = pointSchema.is_external,
            points = CoursePointsOutput.fromPointSchema(pointSchema),
            commits = moduleCommitData
        )
    }
}
