package visdom.fetchers.aplus

import org.mongodb.scala.MongoDatabase
import visdom.fetchers.FetchOptions


abstract class APlusFetchOptions
extends FetchOptions {
    val hostServer: APlusServer
}

final case class GdprOptions(
    exerciseId: Int,
    fieldName: String,
    acceptedAnswer: String
)

abstract class CourseSpecificFetchOptions {
    val courseId: Option[Int]
}

abstract class ModuleSpecificFetchOptions {
    val courseId: Int
    val moduleId: Option[Int]
    val parseNames: Boolean
    val includeExercises: Boolean
}

abstract class ExerciseSpecificFetchOptions {
    val courseId: Int
    val moduleId: Int
    val exerciseId: Option[Int]
    val parseNames: Boolean
    val includeSubmissions: Boolean
}

abstract class SubmissionSpecificFetchOptions {
    val courseId: Int
    val exerciseId: Int
    val submissionId: Option[Int]
    val parseGitAnswers: Boolean
    val useAnonymization: Boolean
}

final case class CourseSpecificFetchParameters(
    courseId: Option[Int]
)
extends CourseSpecificFetchOptions

final case class ModuleSpecificFetchParameters(
    courseId: Int,
    moduleId: Option[Int],
    parseNames: Boolean,
    includeExercises: Boolean,
    gdprOptions: GdprOptions
)
extends ModuleSpecificFetchOptions

final case class ExerciseSpecificFetchParameters(
    courseId: Int,
    moduleId: Int,
    exerciseId: Option[Int],
    parseNames: Boolean,
    includeSubmissions: Boolean,
    gdprOptions: GdprOptions
)
extends ExerciseSpecificFetchOptions

final case class SubmissionSpecificFetchParameters(
    courseId: Int,
    exerciseId: Int,
    submissionId: Option[Int],
    parseGitAnswers: Boolean,
    useAnonymization: Boolean
)
extends SubmissionSpecificFetchOptions

final case class APlusCourseOptions(
    hostServer: APlusServer,
    mongoDatabase: Option[MongoDatabase],
    courseId: Option[Int]
)
extends APlusFetchOptions

final case class APlusModuleOptions(
    hostServer: APlusServer,
    mongoDatabase: Option[MongoDatabase],
    courseId: Int,
    moduleId: Option[Int],
    parseNames: Boolean,
    includeExercises: Boolean,
    gdprOptions: GdprOptions
)
extends APlusFetchOptions

final case class APlusExerciseOptions(
    hostServer: APlusServer,
    mongoDatabase: Option[MongoDatabase],
    courseId: Int,
    moduleId: Int,
    exerciseId: Option[Int],
    parseNames: Boolean,
    includeSubmissions: Boolean,
    gdprOptions: GdprOptions
)
extends APlusFetchOptions

final case class APlusSubmissionOptions(
    hostServer: APlusServer,
    mongoDatabase: Option[MongoDatabase],
    courseId: Int,
    exerciseId: Int,
    submissionId: Option[Int],
    parseGitAnswers: Boolean,
    useAnonymization: Boolean,
    gdprOptions: GdprOptions
)
extends APlusFetchOptions
