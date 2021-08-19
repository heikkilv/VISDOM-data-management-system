package visdom.http.server.fetcher.aplus

import spray.json.JsObject
import visdom.http.server.QueryOptionsBase
import visdom.http.server.services.constants.APlusServerConstants
import visdom.json.JsonUtils


final case class ExerciseDataQueryOptions(
    courseId: String,
    moduleId: String,
    exerciseId: Option[String],
    parseNames: String,
    gdprExerciseId: String,
    gdprFieldName: String,
    gdprAcceptedAnswer: String
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                APlusServerConstants.CourseId -> JsonUtils.toJsonValue(courseId),
                APlusServerConstants.ModuleId -> JsonUtils.toJsonValue(moduleId),
                APlusServerConstants.ExerciseId -> JsonUtils.toJsonValue(exerciseId),
                APlusServerConstants.ParseNames -> JsonUtils.toJsonValue(parseNames),
                APlusServerConstants.GDPRExerciseId -> JsonUtils.toJsonValue((gdprExerciseId)),
                APlusServerConstants.GDPRFieldName -> JsonUtils.toJsonValue((gdprFieldName)),
                APlusServerConstants.GDPRAcceptedAnswer -> JsonUtils.toJsonValue((gdprAcceptedAnswer))
            )
        )
    }
}
