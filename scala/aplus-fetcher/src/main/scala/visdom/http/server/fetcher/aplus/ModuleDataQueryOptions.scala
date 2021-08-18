package visdom.http.server.fetcher.aplus

import spray.json.JsObject
import visdom.http.server.QueryOptionsBase
import visdom.http.server.services.constants.APlusServerConstants
import visdom.json.JsonUtils


final case class ModuleDataQueryOptions(
    courseId: String,
    moduleId: Option[String],
    parseNames: String,
    includeExercises: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                APlusServerConstants.CourseId -> JsonUtils.toJsonValue(courseId),
                APlusServerConstants.ModuleId -> JsonUtils.toJsonValue(moduleId),
                APlusServerConstants.ParseNames -> JsonUtils.toJsonValue(parseNames),
                APlusServerConstants.IncludeExercises -> JsonUtils.toJsonValue(includeExercises)
            )
        )
    }
}
