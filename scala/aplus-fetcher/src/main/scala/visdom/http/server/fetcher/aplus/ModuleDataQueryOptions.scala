package visdom.http.server.fetcher.aplus

import spray.json.JsObject
import visdom.http.server.QueryOptionsBase
import visdom.http.server.services.constants.APlusServerConstants
import visdom.json.JsonUtils


final case class ModuleDataQueryOptions(
    courseId: String,
    moduleId: Option[String],
    parseNames: String,
    includeExercises: String,
    includeSubmissions: String,
    includeGitlabData: String,
    useAnonymization: String,
    gdprExerciseId: Option[String],
    gdprFieldName: String,
    gdprAcceptedAnswer: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                APlusServerConstants.CourseId -> JsonUtils.toJsonValue(courseId),
                APlusServerConstants.ModuleId -> JsonUtils.toJsonValue(moduleId),
                APlusServerConstants.ParseNames -> JsonUtils.toJsonValue(parseNames),
                APlusServerConstants.IncludeExercises -> JsonUtils.toJsonValue(includeExercises),
                APlusServerConstants.IncludeSubmissions -> JsonUtils.toJsonValue(includeSubmissions),
                APlusServerConstants.IncludeGitlabData -> JsonUtils.toJsonValue(includeGitlabData),
                APlusServerConstants.UseAnonymization -> JsonUtils.toJsonValue(useAnonymization),
                APlusServerConstants.GDPRExerciseId -> JsonUtils.toJsonValue((gdprExerciseId)),
                APlusServerConstants.GDPRFieldName -> JsonUtils.toJsonValue((gdprFieldName)),
                APlusServerConstants.GDPRAcceptedAnswer -> JsonUtils.toJsonValue((gdprAcceptedAnswer))
            )
        )
    }
}
