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
    val PathExercises: String = "exercises"

    val ParamFormat: String = "format"

    // constants for the different A+ fetcher types
    val FetcherTypeCourses: String = "courses"
    val FetcherTypeExercises: String = "exercises"
    val FetcherTypeModules: String = "modules"

    val AttributeApiVersion: String = "api_version"
    val AttributeCourseId: String = "course_id"
    val AttributeCourses: String = "courses"
    val AttributeDisplayName: String = "display_name"
    val AttributeExerciseId: String = "exercise_id"
    val AttributeExerciseInfo: String = "exercise_info"
    val AttributeExercises: String = "exercises"
    val AttributeFormI18n: String = "form_i18n"
    val AttributeHierarchicalName: String = "hierarchical_name"
    val AttributeHostName: String = "host_name"
    val AttributeId: String = "id"
    val AttributeLastModified: String = "last_modified"
    val AttributeModuleId: String = "module_id"
    val AttributeModules: String = "modules"
    val AttributeName: String = "name"
    val AttributeNext: String = "next"
    val AttributeParseNames: String = "parse_names"
    val AttributeResults: String = "results"

    // the default wait time for HTTP queries to the A+ API
    val DefaultWaitDurationSeconds: Int = 60
    val DefaultWaitDuration: Duration = Duration(DefaultWaitDurationSeconds, TimeUnit.SECONDS)
}
