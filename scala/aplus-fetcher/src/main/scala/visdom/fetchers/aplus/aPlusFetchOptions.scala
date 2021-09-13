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
    acceptedAnswer: String,
    users: Option[Set[Int]]
) {
    override def toString(): String = {
        (
            exerciseId,
            fieldName,
            acceptedAnswer
        ).toString()
    }
}

abstract class CourseSpecificFetchOptions {
    val courseId: Option[Int]
    val parseNames: Boolean
    val includeModules: Boolean
    val includeExercises: Boolean
    val includeSubmissions: Boolean
    val includePoints: Boolean
    val includeGitlabData: Boolean
    val useAnonymization: Boolean
}

abstract class ModuleSpecificFetchOptions {
    val courseId: Int
    val moduleId: Option[Int]
    val parseNames: Boolean
    val includeExercises: Boolean
    val includeSubmissions: Boolean
    val includeGitlabData: Boolean
    val useAnonymization: Boolean
}

abstract class ExerciseSpecificFetchOptions {
    val courseId: Int
    val moduleId: Option[Int]
    val exerciseId: Option[Int]
    val parseNames: Boolean
    val includeSubmissions: Boolean
    val includeGitlabData: Boolean
    val useAnonymization: Boolean
}

abstract class SubmissionSpecificFetchOptions {
    val courseId: Int
    val exerciseId: Int
    val submissionId: Option[Int]
    val parseGitAnswers: Boolean
    val parseNames: Boolean
    val useAnonymization: Boolean
}

abstract class PointSpecificFetchOptions {
    val courseId: Int
    val userId: Option[Int]
    val parseNames: Boolean
    val useAnonymization: Boolean
}

final case class CourseSpecificFetchParameters(
    courseId: Option[Int],
    parseNames: Boolean,
    includeModules: Boolean,
    includeExercises: Boolean,
    includeSubmissions: Boolean,
    includePoints: Boolean,
    includeGitlabData: Boolean,
    useAnonymization: Boolean,
    gdprOptions: Option[GdprOptions]
)
extends CourseSpecificFetchOptions

final case class ModuleSpecificFetchParameters(
    courseId: Int,
    moduleId: Option[Int],
    parseNames: Boolean,
    includeExercises: Boolean,
    includeSubmissions: Boolean,
    includeGitlabData: Boolean,
    useAnonymization: Boolean,
    gdprOptions: Option[GdprOptions]
)
extends ModuleSpecificFetchOptions

final case class ExerciseSpecificFetchParameters(
    courseId: Int,
    moduleId: Option[Int],
    exerciseId: Option[Int],
    parseNames: Boolean,
    includeSubmissions: Boolean,
    includeGitlabData: Boolean,
    useAnonymization: Boolean,
    gdprOptions: Option[GdprOptions]
)
extends ExerciseSpecificFetchOptions

final case class SubmissionSpecificFetchParameters(
    courseId: Int,
    exerciseId: Int,
    submissionId: Option[Int],
    parseGitAnswers: Boolean,
    parseNames: Boolean,
    useAnonymization: Boolean,
    gdprOptions: GdprOptions
)
extends SubmissionSpecificFetchOptions

final case class PointSpecificFetchParameters(
    courseId: Int,
    userId: Option[Int],
    parseNames: Boolean,
    useAnonymization: Boolean,
    gdprOptions: GdprOptions
)
extends PointSpecificFetchOptions

final case class APlusCourseOptions(
    hostServer: APlusServer,
    mongoDatabase: Option[MongoDatabase],
    courseId: Option[Int],
    parseNames: Boolean,
    includeModules: Boolean,
    includeExercises: Boolean,
    includeSubmissions: Boolean,
    includePoints: Boolean,
    includeGitlabData: Boolean,
    useAnonymization: Boolean,
    gdprOptions: Option[GdprOptions]
)
extends APlusFetchOptions {
    override def toString(): String = {
        (
            courseId,
            parseNames,
            includeModules,
            includeExercises,
            includeSubmissions,
            includePoints,
            includeGitlabData,
            useAnonymization,
            gdprOptions
        ).toString()
    }
}

final case class APlusModuleOptions(
    hostServer: APlusServer,
    mongoDatabase: Option[MongoDatabase],
    courseId: Int,
    moduleId: Option[Int],
    parseNames: Boolean,
    includeExercises: Boolean,
    includeSubmissions: Boolean,
    includeGitlabData: Boolean,
    useAnonymization: Boolean,
    gdprOptions: Option[GdprOptions]
)
extends APlusFetchOptions {
    override def toString(): String = {
        (
            courseId,
            moduleId,
            parseNames,
            includeExercises,
            includeSubmissions,
            includeGitlabData,
            useAnonymization,
            gdprOptions
        ).toString()
    }
}

final case class APlusExerciseOptions(
    hostServer: APlusServer,
    mongoDatabase: Option[MongoDatabase],
    courseId: Int,
    moduleId: Option[Int],
    exerciseId: Option[Int],
    parseNames: Boolean,
    includeSubmissions: Boolean,
    includeGitlabData: Boolean,
    useAnonymization: Boolean,
    gdprOptions: Option[GdprOptions]
)
extends APlusFetchOptions {
    override def toString(): String = {
        (
            courseId,
            moduleId,
            exerciseId,
            parseNames,
            includeSubmissions,
            includeGitlabData,
            useAnonymization,
            gdprOptions
        ).toString()
    }
}

final case class APlusSubmissionOptions(
    hostServer: APlusServer,
    mongoDatabase: Option[MongoDatabase],
    courseId: Int,
    exerciseId: Int,
    submissionId: Option[Int],
    parseGitAnswers: Boolean,
    parseNames: Boolean,
    useAnonymization: Boolean,
    gdprOptions: GdprOptions
)
extends APlusFetchOptions {
    override def toString(): String = {
        (
            courseId,
            exerciseId,
            submissionId,
            parseNames,
            parseGitAnswers,
            useAnonymization,
            gdprOptions
        ).toString()
    }
}

final case class APlusPointOptions(
    hostServer: APlusServer,
    mongoDatabase: Option[MongoDatabase],
    courseId: Int,
    userId: Option[Int],
    parseNames: Boolean,
    useAnonymization: Boolean,
    gdprOptions: GdprOptions
)
extends APlusFetchOptions
