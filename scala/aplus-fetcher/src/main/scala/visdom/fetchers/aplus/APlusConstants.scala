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
    val PathSubmissionData: String = "submissiondata"
    val PathSubmissions: String = "submissions"

    val ParamFormat: String = "format"

    // constants for the different A+ fetcher types
    val FetcherTypeCourses: String = "courses"
    val FetcherTypeExercises: String = "exercises"
    val FetcherTypeModules: String = "modules"
    val FetcherTypeSubmissions: String = "submission"

    val AttributeAcceptedAnswer: String = "accepted_answer"
    val AttributeApiVersion: String = "api_version"
    val AttributeCourseId: String = "course_id"
    val AttributeCourses: String = "courses"
    val AttributeDisplayName: String = "display_name"
    val AttributeEmail: String = "email"
    val AttributeExerciseId: String = "exercise_id"
    val AttributeExerciseInfo: String = "exercise_info"
    val AttributeExercises: String = "exercises"
    val AttributeFieldName: String = "field_name"
    val AttributeFormI18n: String = "form_i18n"
    val AttributeFullName: String = "full_name"
    val AttributeGdprOptions: String = "gdpr_options"
    val AttributeGitHostName: String = "git_host_name"
    val AttributeGitProjectName: String = "git_project_name"
    val AttributeHierarchicalName: String = "hierarchical_name"
    val AttributeHostName: String = "host_name"
    val AttributeId: String = "id"
    val AttributeLastModified: String = "last_modified"
    val AttributeModuleId: String = "module_id"
    val AttributeModules: String = "modules"
    val AttributeName: String = "name"
    val AttributeNext: String = "next"
    val AttributeParseGitAnswers: String = "parse_git_answers"
    val AttributeParseNames: String = "parse_names"
    val AttributeResults: String = "results"
    val AttributeStudentId: String = "student_id"
    val AttributeSubmissionData: String = "submission_data"
    val AttributeSubmissionId: String = "submission_id"
    val AttributeSubmitters: String = "submitters"
    val AttributeUseAnonymization: String = "use_anonymization"
    val AttributeUsername: String = "username"

    // the default wait time for HTTP queries to the A+ API
    val DefaultWaitDurationSeconds: Int = 60
    val DefaultWaitDuration: Duration = Duration(DefaultWaitDurationSeconds, TimeUnit.SECONDS)
}
