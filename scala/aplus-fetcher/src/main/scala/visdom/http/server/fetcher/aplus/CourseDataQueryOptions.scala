package visdom.http.server.fetcher.aplus

import spray.json.JsObject
import visdom.http.server.QueryOptionsBase
import visdom.http.server.services.constants.APlusServerConstants
import visdom.json.JsonUtils


final case class CourseDataQueryOptions(
    courseId: Option[String]
) extends QueryOptionsBase {
    def toJsObject(): JsObject = {
        JsObject(
            Map(
                APlusServerConstants.AttributeCourseId -> JsonUtils.toJsonValue(courseId)
            )
        )
    }
}
