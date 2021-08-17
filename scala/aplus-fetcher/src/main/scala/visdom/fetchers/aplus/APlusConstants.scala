package visdom.fetchers.aplus

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration


object APlusConstants {
    // the base path for the A+ API
    val PathBase: String = "api/v2"

    // the A+ API version
    val APlusApiVersion: Int = 2

    val HeaderAuthorization: String = "Authorization"
    val Token: String = "Token"

    val PathCourses: String = "courses"

    val ParamFormat: String = "format"

    // constants for the different A+ fetcher types
    val FetcherTypeCourses: String = "courses"

    val AttributeApiVersion: String = "api_version"
    val AttributeCourseId: String = "course_id"
    val AttributeHostName: String = "host_name"
    val AttributeId: String = "id"
    val AttributeLastModified: String = "last_modified"
    val AttributeNext: String = "next"
    val AttributeResults: String = "results"

    // the default wait time for HTTP queries to the A+ API
    val DefaultWaitDurationSeconds: Int = 60
    val DefaultWaitDuration: Duration = Duration(DefaultWaitDurationSeconds, TimeUnit.SECONDS)
}
