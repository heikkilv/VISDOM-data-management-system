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
    val PathPoints: String = "points"
    val PathStudents: String = "students"
    val PathSubmissionData: String = "submissiondata"
    val PathSubmissions: String = "submissions"

    val ParamFormat: String = "format"

    // constants for the different A+ fetcher types
    val FetcherTypeCourses: String = "courses"
    val FetcherTypeExercises: String = "exercises"
    val FetcherTypeModules: String = "modules"
    val FetcherTypePoints: String = "points"
    val FetcherTypeSubmissions: String = "submissions"

    val AttributeAcceptedAnswer: String = "accepted_answer"
    val AttributeApiVersion: String = "api_version"
    val AttributeCourseId: String = "course_id"
    val AttributeCourses: String = "courses"
    val AttributeDisplayName: String = "display_name"
    val AttributeEmail: String = "email"
    val AttributeEndDate: String = "end_date"
    val AttributeExercise: String = "exercise"
    val AttributeExerciseId: String = "exercise_id"
    val AttributeExerciseInfo: String = "exercise_info"
    val AttributeExercises: String = "exercises"
    val AttributeFieldName: String = "field_name"
    val AttributeFormI18n: String = "form_i18n"
    val AttributeFullName: String = "full_name"
    val AttributeGdprOptions: String = "gdpr_options"
    val AttributeGrader: String = "grader"
    val AttributeHierarchicalName: String = "hierarchical_name"
    val AttributeHostName: String = "host_name"
    val AttributeId: String = "id"
    val AttributeIncludeExercises: String = "include_exercises"
    val AttributeIncludeGitlabData: String = "include_gitlab_data"
    val AttributeIncludeModules: String = "include_modules"
    val AttributeIncludePoints: String = "include_points"
    val AttributeIncludeSubmissions: String = "include_submissions"
    val AttributeIsFolder: String = "is_folder"
    val AttributeLanguage: String = "language"
    val AttributeLastModified: String = "last_modified"
    val AttributeLateSubmissionCoefficient: String = "late_submission_coefficient"
    val AttributeLateSubmissionDate: String = "late_submission_date"
    val AttributeModuleId: String = "module_id"
    val AttributeModules: String = "modules"
    val AttributeName: String = "name"
    val AttributeNext: String = "next"
    val AttributeNumber: String = "number"
    val AttributeOther: String = "other"
    val AttributeParseGitAnswers: String = "parse_git_answers"
    val AttributeParseNames: String = "parse_names"
    val AttributePath: String = "path"
    val AttributePoints: String = "points"
    val AttributeProjectName: String = "project_name"
    val AttributeRaw: String = "raw"
    val AttributeResults: String = "results"
    val AttributeStartDate: String = "start_date"
    val AttributeStudentId: String = "student_id"
    val AttributeSubmissionData: String = "submission_data"
    val AttributeSubmissionId: String = "submission_id"
    val AttributeSubmissions: String = "submissions"
    val AttributeSubmitters: String = "submitters"
    val AttributeUseAnonymization: String = "use_anonymization"
    val AttributeUserId: String = "user_id"
    val AttributeUsername: String = "username"

    // the default wait time for HTTP queries to the A+ API
    val DefaultWaitDurationSeconds: Int = 60
    val DefaultWaitDuration: Duration = Duration(DefaultWaitDurationSeconds, TimeUnit.SECONDS)

    val ErrorForExerciseId: String = "error"
}
